package com.example.musiclan;


import java.util.ArrayList;

import com.example.musiclan.R;
import com.example.musiclan.R.layout;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class PeerTopFragment extends Fragment{
	
	private View mContentView = null;
	private Button btnReqList; 
	ListView listView ;
	Context context;
	@Override
   public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
      /**
       * Inflate the layout for this fragment
       */
    //  return inflater.inflate(R.layout.peer_top_fragment, container, false);
      context = getActivity();
      mContentView = inflater.inflate(R.layout.peer_top_fragment, container, false);
    
      listView = (ListView)mContentView.findViewById(R.id.list);
      
      final ArrayList<SongList> listOfSongs = DeviceDetailFragment.listOfSongs;
      
      btnReqList = (Button) mContentView.findViewById(R.id.btn_Req_List);
      btnReqList.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			  // Get ListView object from xml
           
            
            // Defined Array values to show in ListView
            String[] values = new String[listOfSongs.size()];
            for(int i=0;i<listOfSongs.size();i++){
            	SongList s = listOfSongs.get(i); 
            	values[i] = s.song;
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1, android.R.id.text1, values);
                    
                  // Assign adapter to ListView
            listView.setAdapter(adapter); 
			
		}
	});
      
      // ListView Item Click Listener
      listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
              
             // ListView Clicked item index
             int itemPosition = position;
             
             // ListView Clicked item value
             String  itemValue = (String) listView.getItemAtPosition(position);
                
              // Show Alert 
              Toast.makeText(context,"Position :"+itemPosition+"  Song : " + itemValue , Toast.LENGTH_SHORT).show();
           
            }

			

       }); 
      
      return mContentView;
   }
	
	
}