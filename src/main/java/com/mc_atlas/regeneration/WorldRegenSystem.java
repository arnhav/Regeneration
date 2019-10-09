package com.mc_atlas.regeneration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.World;

public class WorldRegenSystem {

	private World target;
	private World template;
	private int maxX, maxZ, minX, minZ;
	private Connection conn ;
	
	public WorldRegenSystem(Connection conn, World target, World template, int maxX, int maxZ, int minX, int minZ)
	{
		if(target == template)
			throw new RuntimeException("template world and target cannot be the same");
		
		this.target = target;
		this.template = template;
		this.maxX = maxX;
		this.maxZ = maxZ;
		this.minX = minX;
		this.minZ = minZ;
		this.conn = conn;
	}
	
	public void loadNextBatch()
	{
		try(PreparedStatement stm = conn.prepareStatement("SELECT ChunkX, ChunkZ "
				+ "FROM ChunkStatus "
				+ "WHERE ChunkX >= ? AND ChunkX <= ? AND ChunkZ >= ? AND ChunkX <= ? AND WorldName = ? "
				+ "LIMIT 100"))
		{
			stm.setInt(1, minX);
			stm.setInt(2, maxX);
			stm.setInt(3, minZ);
			stm.setInt(4, maxZ);
			stm.setString(5, target.getName());
		}
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
