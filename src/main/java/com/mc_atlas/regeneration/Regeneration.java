package com.mc_atlas.regeneration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import co.bronet.machinations.api.IPlot;
import co.bronet.machinations.api.MachinationsService;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.destroystokyo.paper.HeightmapType;

public class Regeneration extends JavaPlugin implements Listener
{
	Random rand = new Random();
	private LinkedBlockingQueue<Chunk> blocksChanged = new LinkedBlockingQueue<Chunk>(); //TODO consider ConcurrentLinkedQueue
	private HashMap<World, World> realToTemplate = new HashMap<World, World> ();
	
	private LinkedBlockingQueue<ChunkTask> toDoList = new LinkedBlockingQueue<ChunkTask>();
	private LinkedBlockingQueue<ChunkTask> doneList = new LinkedBlockingQueue<ChunkTask>();
	
	private Connection conn;

	private MachinationsService nationsAPI;

	private FileConfiguration config;
	
	// A chunk will not be reconsidered until after this amount of time has passed.
	// set this to a few hours
	public int minSecondsBetweenChunkConsideration;
	
	// The amount of time after a player has placed or broken blocks before the regen system will consider it for regen.
	// This should be a few days or so.
	public int minPlayerBuildSeconds;
	
	// Once a chunk has had regen passed over it, wait at least this many seconds before doing another pass.
	// Set this to a few minutes
	public int minSecondsBetweenChunkRegenPass;
	
	private void doUpdate(String sql) throws SQLException
	{
		try(PreparedStatement stm = conn.prepareStatement(sql))
		{
			stm.executeUpdate();
		}
	}
	
	// could probably replace with IPlot, but we may want to add extra attributes later.
	private class ChunkTask
	{
		int x,z;
		String worldName;
		public String toString()
		{
			return worldName+" "+x+" "+z;
		}
	}
	private void openDatabase() throws SQLException
	{
		File f = new File(this.getDataFolder(), "Regen.db");
		f.getParentFile().mkdirs();
		String file = f.getAbsolutePath();
		String url = "jdbc:sqlite:"+file;
		conn = DriverManager.getConnection(url);
		
		doUpdate("CREATE TABLE IF NOT EXISTS ChunkStatus ( "
				+ "WorldName TEXT NOT NULL, "
				+ "ChunkX INT NOT NULL, "
				+ "ChunkZ INT NOT NULL,"
				+ "LastPlayerVisitTime LONG DEFAULT 0, "
				+ "LastPlayerEditTime LONG DEFAULT 0, "
				+ "LastRegenPassTime LONG DEFAULT 0, "
				+ "LastRegenConsiderTime LONG DEFAULT 0, "
				+ "Priority LONG DEFAULT 0, "
				+ "PRIMARY KEY (WorldName, ChunkX, ChunkZ)) ");

	}

	public void createConfig() {
		try {
			if (!getDataFolder().exists()) {
				getDataFolder().mkdirs();
			}
			File file = new File(getDataFolder(), "config.yml");
			config = new YamlConfiguration();
			if (!file.exists()) {
				this.getLogger().info("config.yml not found, creating...");
				saveDefaultConfig();
			} else {
				this.getLogger().info("config.yml found, loading...");
				loadConfig();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadConfig(){
		config = this.getConfig();
		minSecondsBetweenChunkConsideration = config.getInt("minSecondsBetweenChunkConsideration");
		minPlayerBuildSeconds = config.getInt("minPlayerBuildSeconds");
		minSecondsBetweenChunkRegenPass = config.getInt("minSecondsBetweenChunkRegenPass");
	}

	/**
	 *
	 * OnEnable
	 *
	 */
	@Override
	public void onEnable()
	{
		this.getLogger().info("Starting");

		nationsAPI = getNationsSystem();
		
		try {
			openDatabase();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		createConfig();
		
		realToTemplate.put(Bukkit.getWorld("Athera"), Bukkit.getWorld("AtheraCopy"));
		
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> asyncDatabaseUpdate(), 200, 200);
		Bukkit.getScheduler().runTaskTimer(this, () -> processOneChunk(), 200, 200);
		
		Bukkit.getPluginManager().registerEvents(this, this);
	}

	private MachinationsService getNationsSystem() {
		return Bukkit.getServer().getServicesManager().load(MachinationsService.class);
	}
	
	private void asyncDatabaseUpdate() {
		commitPlayerChanges();
		loadNextBatch();
		commitRegenPass();
	}
	private boolean prob(double percent)
	{
		return rand.nextDouble() * 100 < percent;
	}
	
	private Block recurseDown(Block b)
	{
		while(b.isEmpty() || b.isLiquid())
			b = b.getRelative(BlockFace.DOWN);
		return b.getRelative(BlockFace.UP);
	}
	
	private void onChunkSyncReady(Chunk real, Chunk templ, ChunkTask ct)
	{
		System.out.println("Attempting regen " + real);
		for(int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int realMax = real.getWorld().getHighestBlockYAt(x + real.getX() * 16, z + real.getZ() * 16, HeightmapType.ANY);
				int tempMax = templ.getWorld().getHighestBlockYAt(x + templ.getX() * 16, z + templ.getZ() * 16, HeightmapType.ANY);
				int max = Math.max(realMax, tempMax);
				for (int y = max; y > 0; y--) {
					Block rb = real.getBlock(x, y, z);
					Block tb = templ.getBlock(x, y, z);
					Material rm = rb.getType();
					Material tm = tb.getType();
					if (rm == tm) continue;

					Block fallenBlock = RuinificationUtil.makeBlockFall(rb);

					if (prob(50)) {
						RuinificationUtil.ruinifyBlock(fallenBlock);
					}

				}

				for (int y = 0; y < max; y++) {
					Block rb = real.getBlock(x, y, z);
					Block tb = templ.getBlock(x, y, z);
					Material rm = rb.getType();
					Material tm = tb.getType();
					if (rm == tm) continue;

					if (rb.isEmpty() && !tb.isEmpty()) {
						if (prob(50)) {
							rb.setType(tb.getType());
							break;
						}
					}
				}
			}
		}
		try {
			doneList.put(ct);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private boolean arePlayersNear(World w, int chunkx, int chunkz)
	{
		int chunkMinDist = 10;
		for(Player p : Bukkit.getOnlinePlayers())
		{
			Chunk cp = p.getLocation().getChunk();
			int xdiff = chunkx - cp.getX();
			int zdiff = chunkz - cp.getZ();
			if(  zdiff*zdiff + xdiff*xdiff < chunkMinDist*chunkMinDist ) return true;
		}
		return false;
	}
	
	private boolean isTownHere(ChunkTask obk)
	{
		IPlot plot = IPlot.fromChunkCoordinates(obk.x, obk.z, obk.worldName);
		return nationsAPI.getTownByPlot(plot) != null;
	}
	// called sync
	private void processOneChunk()
	{
		ChunkTask obk = toDoList.poll();
		if(obk == null) return;
		
		World ww = Bukkit.getWorld(obk.worldName);
		if(ww == null) return;
		
		// this world does have a template copy?
		World template = this.realToTemplate.get(ww);
		if(template == null) return;
		
		// skip the chunk is players are near by 
		if(arePlayersNear(ww, obk.x, obk.z)) return;
		
		// check if there is a town here and return if there is.
		if(isTownHere(obk)) return;		
		
		// TODO fire an event here to allow plugins to reject the regen
		
		// load both real and template chunks async
		template.getChunkAtAsync(obk.x, obk.z, false).thenAccept(t -> 
			ww.getChunkAtAsync(obk.x, obk.z, false).thenAccept(c -> 
				onChunkSyncReady(c,t, obk)
		));
	}
	
	// called from async thread
	private void loadNextBatch() {
		// wait until we have no more chunks to consider. 
		if(!toDoList.isEmpty()) return;
				
		long now = System.currentTimeMillis();
		
		// fetch some chunks we have no considered for a while, and have not been visited for some time
		// mark them as being considered. 
		// TODO do we actually care about when a player visits?
		try(PreparedStatement stm2 = conn.prepareStatement("" +
				"UPDATE ChunkStatus \n" +
				"SET LastRegenConsiderTime = ? " +
				"WHERE rowid in (" +
				"    SELECT rowid FROM ChunkStatus " +
				"    WHERE LastRegenConsiderTime < ? AND LastPlayerEditTime < ? AND LastRegenPassTime < ? " +
				"    ORDER BY LastRegenConsiderTime " +
				"    LIMIT 100)")) {
			stm2.setLong(1, now);
			stm2.setLong(2, now - 1000*minSecondsBetweenChunkConsideration);			
			stm2.setLong(3, now - 1000*minPlayerBuildSeconds);
			stm2.setLong(4, now - 1000*minSecondsBetweenChunkRegenPass);
			stm2.executeUpdate();

			// select the ones we just updated.
			try(PreparedStatement stm = conn.prepareStatement(""
					+ "SELECT ChunkX, ChunkZ, WorldName FROM ChunkStatus "
					+ "WHERE LastRegenConsiderTime = ?")) {

				stm.setLong(1, now);
				ResultSet rs = stm.executeQuery();
				while(rs.next())
				{
					ChunkTask nn = new ChunkTask();
					nn.x = rs.getInt("ChunkX");
					nn.z = rs.getInt("ChunkZ");
					nn.worldName = rs.getString("WorldName");
					toDoList.put(nn);
					System.out.println("Select a chunk " + nn);
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// called from async thread
	private void commitRegenPass() 
	{
		if(doneList.isEmpty()) return;
		ChunkTask ct ;

		try(PreparedStatement stm = conn.prepareStatement(""
				+ "INSERT INTO ChunkStatus "
				+ "(WorldName, ChunkX, ChunkZ, LastRegenPassTime ) "
				+ "VALUES (?,?,?,?) ON CONFLICT (WorldName, ChunkX, ChunkZ) DO UPDATE SET LastRegenPassTime = ? "))
		{
			stm.setLong(4, System.currentTimeMillis());
			stm.setLong(5, System.currentTimeMillis());

			while( (ct = doneList.poll()) != null)
			{
				stm.setString(1, ct.worldName);
				stm.setInt(2, ct.x);
				stm.setInt(3, ct.z);
				stm.executeUpdate();
				System.out.println("Update LastRegenPassTime " + ct);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	// called from async thread
	private void commitPlayerChanges() {
		if(blocksChanged.isEmpty()) return;
		HashSet<Chunk> seen = new HashSet<Chunk>();
		try(PreparedStatement stm = conn.prepareStatement(""
				+ "INSERT INTO ChunkStatus "
				+ "(WorldName, ChunkX, ChunkZ, LastPlayerVisitTime, LastPlayerEditTime ) "
				+ "VALUES (?,?,?,?,?) ON CONFLICT (WorldName, ChunkX, ChunkZ) DO UPDATE SET LastPlayerVisitTime = ?, LastPlayerEditTime = ? "))
		{
			stm.setLong(4, System.currentTimeMillis());
			stm.setLong(5, System.currentTimeMillis());
			stm.setLong(6, System.currentTimeMillis());
			stm.setLong(7, System.currentTimeMillis());
			Chunk ch = null;
			while( (ch = blocksChanged.poll()) != null) {
				// prevent duplicate updates
				if(seen.add(ch)) {
					stm.setString(1, ch.getWorld().getName());
					stm.setInt(2, ch.getX());
					stm.setInt(3, ch.getZ());
					System.out.println("Commit player change " + ch.getX()+" "+ch.getZ());
					stm.executeUpdate();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// called sync
	private void logBlockChange(Block b)
	{
		if(realToTemplate.containsKey(b.getWorld()))
			blocksChanged.add(b.getChunk());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent eb)
	{
		logBlockChange(eb.getBlock());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlaceEvent(BlockPlaceEvent eb)
	{
		logBlockChange(eb.getBlock());	
	}
}
