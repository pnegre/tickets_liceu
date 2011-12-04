package com.liceu.tickets;

import java.lang.Void;
import java.util.*;
import android.util.*;
import android.os.AsyncTask;

import android.app.*;
import android.os.Bundle;
import android.widget.*;
import android.view.View;
import android.content.Intent;
import android.text.InputType;
import android.widget.ListView;



public class ViewTicketActivity extends Activity
{
	TicketsApp tapp;
	
	Button btclose;
	Button btsend;
	TextView text;
	TextView reporter;
	TextView place;
	ListView commentslist;
	EditText tcomment;
	CheckBox checkemail;
	
	Ticket ticket;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		tapp = (TicketsApp) getApplication();
		
		// Get extra information passed to activity
		Bundle extras = getIntent().getExtras();
		int tickid = extras.getInt("tickid");
		ticket = tapp.database.getTicket(tickid);
		
		setContentView(R.layout.ticketview);
		
		commentslist = (ListView) findViewById(R.id.commentslist);
		View header1 =  getLayoutInflater().inflate(R.layout.ticketviewheader, null, false);
		commentslist.addHeaderView(header1, null, false);
		
		text = (TextView) findViewById(R.id.tickettext);
		reporter = (TextView) findViewById(R.id.reporter);
		place = (TextView) findViewById(R.id.place);
		btclose = (Button) findViewById(R.id.btclose);
		btsend = (Button) findViewById(R.id.btsend);
		tcomment = (EditText) findViewById(R.id.tcomment);
		checkemail = (CheckBox) findViewById(R.id.checkemail);
		
		btclose.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new ataskCloseTicket().execute();
			}
		});
		
		btsend.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new ataskSendComment().execute();
			}
		});
		
		text.setText(ticket.text);
		reporter.setText(ticket.user);
		place.setText(ticket.place);
		
		writeComments();
	}
	
	
	void writeComments()
	{
		List comments = tapp.database.getComments(ticket.id);
		commentslist.setAdapter(new ArrayAdapter<List>(this, R.layout.list_item, comments));
	}
	
	
	// ******************************
	// Async task for closing tickets
	class ataskCloseTicket extends AsyncTask<Void,Void,Void> {
		ProgressDialog pb;
		
		protected void onPreExecute() {
			pb = ProgressDialog.show(ViewTicketActivity.this, "", "Tancant...", true);
		}
		
		protected Void doInBackground(Void... params) {
			try
			{
				Map<String,String> vars = new HashMap<String,String>();
				vars.put("action","close");
				String url = new String("https://apps.esliceu.com/tickets/ticket/"
					+ String.valueOf(ticket.id));
				tapp.server.doPost(url, vars);
				tapp.database.removeTicket(ticket.id);
			}
			catch (ServerError e) { }
			
			return null;
		}
		
		protected void onPostExecute(Void param) {
			pb.dismiss();
			ViewTicketActivity.this.finish();
		}
	}
	
	
	// *******************************
	// Async task for sending comments
	class ataskSendComment extends AsyncTask<Void,Void,Void> {
		ProgressDialog pb;
		
		protected void onPreExecute() {
			pb = ProgressDialog.show(ViewTicketActivity.this, "", "Enviant...", true);
		}
		
		protected Void doInBackground(Void... params) {
			try
			{
				String text = tcomment.getText().toString();
				if (text.equals("")) return null;
				
				Map<String,String> vars = new HashMap<String,String>();
				vars.put("action","new");
				vars.put("text", text);
				
				if (checkemail.isChecked())
					vars.put("email","ON");
				
				String url = new String("https://apps.esliceu.com/tickets/ticket/" + String.valueOf(ticket.id));
				tapp.server.doPost(url, vars);
				tapp.database.addComment(new Comment(text,"aa"), ticket.id);
			}
			catch (ServerError e) { }
			
			return null;
		}
		
		protected void onPostExecute(Void param) {
			writeComments();
			tcomment.setText("");
			checkemail.setChecked(false);
			pb.dismiss();
		}
	}

}
