package com.liceu.tickets;

import java.util.*;


import android.preference.PreferenceActivity;
import android.os.Bundle;


public class TicketsPreferences extends PreferenceActivity 
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}
}
