package com.skriptlang.scroll.objects;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public record Location(Vec3d vector, World world) {

	public Vec3d getVector() {
		return vector;
	}

	public World getWorld() {
		return world;
	}

	public double getX() {
		return vector.getX();
	}
	
	public double getY() {
		return vector.getY();
	}
	
	public double getZ() {
		return vector.getZ();
	}

}
