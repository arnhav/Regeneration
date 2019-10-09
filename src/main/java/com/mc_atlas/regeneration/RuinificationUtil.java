package com.mc_atlas.regeneration;


import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.inventory.InventoryHolder;

public class RuinificationUtil {

	public static Block ruinifyBlock(Block block) {
		block.setType(getRuinfiedMaterial(block.getType()), false);
		return block;
	}
	
	//TODO Need to convert this to config file system so the ruinification blocks can be changed and adjusted more easily.
	public static Material getRuinfiedMaterial(Material mat) {
		switch(mat) {
		case STONE:
			return Material.COBBLESTONE;
		case COBBLESTONE:
			return splitChance(Material.COBBLESTONE_SLAB,Material.AIR);
		case MOSSY_COBBLESTONE:
			return splitChance(Material.COBBLESTONE_SLAB,Material.AIR);
		case POLISHED_GRANITE:
			return Material.GRANITE;
		case POLISHED_DIORITE:
			return Material.DIORITE;
		case POLISHED_ANDESITE:
			return Material.ANDESITE;
		case OAK_LOG:
			return Material.STRIPPED_OAK_LOG;
		case SPRUCE_LOG:
			return Material.STRIPPED_SPRUCE_LOG;
		case BIRCH_LOG:
			return Material.STRIPPED_BIRCH_LOG;
		case JUNGLE_LOG:
			return Material.STRIPPED_JUNGLE_LOG;
		case ACACIA_LOG:
			return Material.STRIPPED_ACACIA_LOG;
		case DARK_OAK_LOG:
			return Material.STRIPPED_DARK_OAK_LOG;
		case OAK_WOOD:
			return Material.STRIPPED_OAK_WOOD;
		case SPRUCE_WOOD:
			return Material.STRIPPED_SPRUCE_WOOD;
		case BIRCH_WOOD:
			return Material.STRIPPED_BIRCH_WOOD;
		case JUNGLE_WOOD:
			return Material.STRIPPED_JUNGLE_WOOD;
		case ACACIA_WOOD:
			return Material.STRIPPED_ACACIA_WOOD;
		case DARK_OAK_WOOD:
			return Material.STRIPPED_DARK_OAK_WOOD;
		case OAK_PLANKS:
			return splitChance(Material.OAK_SLAB,Material.AIR);
		case SPRUCE_PLANKS:
			return splitChance(Material.SPRUCE_SLAB,Material.AIR);
		case BIRCH_PLANKS:
			return splitChance(Material.BIRCH_SLAB,Material.AIR);
		case JUNGLE_PLANKS:
			return splitChance(Material.JUNGLE_SLAB,Material.AIR);
		case ACACIA_PLANKS:
			return splitChance(Material.ACACIA_SLAB,Material.AIR);
		case DARK_OAK_PLANKS:
			return splitChance(Material.DARK_OAK_SLAB,Material.AIR);
		case SANDSTONE:
			return Material.SAND;
		case CHISELED_SANDSTONE:
			return Material.SANDSTONE;
		case CUT_SANDSTONE:
			return Material.SANDSTONE;
		case SMOOTH_SANDSTONE:
			return Material.SANDSTONE;
		case RED_SANDSTONE:
			return Material.RED_SAND;
		case CHISELED_RED_SANDSTONE:
			return Material.RED_SANDSTONE;
		case CUT_RED_SANDSTONE:
			return Material.RED_SANDSTONE;
		case SMOOTH_RED_SANDSTONE:
			return Material.RED_SANDSTONE;
		case BLACK_CONCRETE:
			return Material.BLACK_CONCRETE_POWDER;
		case BLUE_CONCRETE:
			return Material.BLUE_CONCRETE_POWDER;
		case BROWN_CONCRETE:
			return Material.BROWN_CONCRETE_POWDER;
		case CYAN_CONCRETE:
			return Material.CYAN_CONCRETE_POWDER;
		case GRAY_CONCRETE:
			return Material.GRAY_CONCRETE_POWDER;
		case GREEN_CONCRETE:
			return Material.GREEN_CONCRETE_POWDER;
		case LIGHT_BLUE_CONCRETE:
			return Material.LIGHT_BLUE_CONCRETE_POWDER;
		case LIGHT_GRAY_CONCRETE:
			return Material.LIGHT_GRAY_CONCRETE_POWDER;
		case LIME_CONCRETE:
			return Material.LIME_CONCRETE_POWDER;
		case MAGENTA_CONCRETE:
			return Material.MAGENTA_CONCRETE_POWDER;
		case ORANGE_CONCRETE:
			return Material.ORANGE_CONCRETE_POWDER;
		case PINK_CONCRETE:
			return Material.PINK_CONCRETE_POWDER;
		case PURPLE_CONCRETE:
			return Material.PURPLE_CONCRETE_POWDER;
		case RED_CONCRETE:
			return Material.RED_CONCRETE_POWDER;
		case WHITE_CONCRETE:
			return Material.WHITE_CONCRETE_POWDER;
		case YELLOW_CONCRETE:
			return Material.YELLOW_CONCRETE_POWDER;	
		case BRICKS:
			return splitChance(Material.BRICKS,Material.BRICK_SLAB);
		case BRICK_SLAB:
			return splitChance(Material.AIR,Material.BRICK_SLAB);
		case CHISELED_STONE_BRICKS:
			return Material.SMOOTH_STONE;
		case SMOOTH_STONE:
			return Material.STONE;
		case STONE_BRICKS:
			return splitChance(Material.CRACKED_STONE_BRICKS,Material.MOSSY_STONE_BRICKS);
		case CRACKED_STONE_BRICKS:
			return splitChance(Material.CRACKED_STONE_BRICKS,Material.COBBLESTONE);
		case MOSSY_STONE_BRICKS:
			return splitChance(Material.MOSSY_COBBLESTONE,Material.MOSSY_STONE_BRICKS);
		case QUARTZ_BLOCK:
			return Material.WHITE_CONCRETE_POWDER;
		case QUARTZ_PILLAR:
			return Material.WHITE_CONCRETE_POWDER;
		case CHISELED_QUARTZ_BLOCK:
			return Material.WHITE_CONCRETE_POWDER;
		case SMOOTH_QUARTZ:
			return Material.WHITE_CONCRETE_POWDER;
		case DARK_PRISMARINE:
			return Material.PRISMARINE_BRICKS;
		case PRISMARINE_BRICKS:
			return Material.PRISMARINE;
		default:
			return Material.AIR;
		}
	}
	
	private static Material splitChance(Material mat1, Material mat2) {
		Random random = new Random();
		if(random.nextBoolean()) {
			return mat1;
		}
		return mat2;
	}
	
	// Makes a block fall to the ground to get less floating blocks during reclamation
	public static Block makeBlockFall(Block floatingBlock) {
		Block block = floatingBlock;
		Material mat = floatingBlock.getType();
		while(block.getRelative(BlockFace.DOWN).getType() == Material.AIR ||
				block.getRelative(BlockFace.DOWN).getType() == Material.WATER) {
			block = block.getRelative(BlockFace.DOWN);
		}
		
		if(floatingBlock.getRelative(BlockFace.EAST).getType().equals(Material.WATER) && 
				floatingBlock.getRelative(BlockFace.WEST).getType().equals(Material.WATER) &&
				floatingBlock.getRelative(BlockFace.NORTH).getType().equals(Material.WATER) && 
				floatingBlock.getRelative(BlockFace.SOUTH).getType().equals(Material.WATER)) {
			floatingBlock.setType(Material.WATER);
		} else {
			floatingBlock.setType(Material.AIR);
		}
		
		block.setType(mat);
		return block;
	}
	
	//TODO
	// Attempts to add vines to the blocks randomly in teh ruinification process. 
	// Caused some errors, needs to be re-examined and redone.
	public static void MakeVine(Block block) {
		try  {
			Random random = new Random();
			if(random.nextInt(10) == 5) {
				block.setType(Material.VINE);
				MultipleFacing vine = (MultipleFacing) block.getBlockData();
				
				if(!block.getRelative(BlockFace.NORTH).getType().equals(Material.AIR)) {
					vine.setFace(BlockFace.NORTH, true);
				}
				if(!block.getRelative(BlockFace.SOUTH).getType().equals(Material.AIR)) {
					vine.setFace(BlockFace.SOUTH, true);
				}
				if(!block.getRelative(BlockFace.EAST).getType().equals(Material.AIR)) {
					vine.setFace(BlockFace.EAST, true);
				}
				if(!block.getRelative(BlockFace.WEST).getType().equals(Material.AIR)) {
					vine.setFace(BlockFace.WEST, true);
				}
				
				block.setBlockData(vine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
