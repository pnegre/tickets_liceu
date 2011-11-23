package com.liceu.tickets;

import java.util.*;
import android.util.Log;


class Comment
{
	String text;
	String author;
	
	Comment(String t, String a)
	{
		text = t; author = a;
	}
	
	public String toString()
	{
		return text;
	}
}

class Ticket
{
	String place;
	String text;
	String user;
	int id;
	
	Ticket(int i, String t, String u, String p)
	{
		id = i;
		text = t;
		user = u;
		place = p;
	}
	
	public String toString()
	{
		if (text.length() > 50)
			return text.substring(0,50);
		else
			return text;
	}
	
}


