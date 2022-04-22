package com.gadarts.necronemes.systems.render;

import com.gadarts.necronemes.systems.render.DrawFlags;

public interface DrawFlagSet {
	void run(DrawFlags drawFlags, boolean value);
}
