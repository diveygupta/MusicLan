package com.example.musiclan;

import java.io.Serializable;

public class SongSelection implements Serializable {

	boolean setPause;
	boolean setPlay;
	String songPath;
	
	SongSelection()
	{
		this.setPause = false;
		this.setPlay = true;
		songPath = null;
	}
}
