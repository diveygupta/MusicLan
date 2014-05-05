package com.example.musiclan;

import java.io.Serializable;

public class SongSelection implements Serializable {

	boolean setPause;
	boolean setPlay;
	boolean setDownload;
	String songPath;
	
	SongSelection()
	{
		this.setPause = false;
		this.setPlay = false;
		this.setDownload = false;
		songPath = null;
	}
}
