package ro.pub.cs.systems.eim.practicaltest02;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;




import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;

import com.example.ro.pub.cs.systems.eim.practicaltest02.R;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class PracticalTest02MainActivity extends Activity {

	 private TextView clientTextView;
	    // Server widgets
	    private EditText serverPortEditText = null;
	    private Button connectButton = null;

	    // Client widgets
	    private EditText clientAddressEditText = null;
	    private EditText clientPortEditText = null;
	    private EditText cityEditText = null;
	    private Button getWeatherForecastButton = null;

	    private ServerTextContentWatcher serverTextContentWatcher = new ServerTextContentWatcher();
	    private class ServerTextContentWatcher implements TextWatcher {

	        @Override
	        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
	        }

	        @Override
	        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
	            Log.v(Constants.TAG, "Text changed in edit text: " + charSequence.toString());
	            if (Constants.SERVER_START.equals(charSequence.toString())) {
	                //serverThread = new ServerThread();
	                //serverThread.startServer();
	                //Log.v(Constants.TAG, "Starting server...");
	            }
	            if (Constants.SERVER_STOP.equals(charSequence.toString())) {
	                //serverThread.stopServer();
	                Log.v(Constants.TAG, "Stopping server...");
	            }
	        }

	        @Override
	        public void afterTextChanged(Editable editable) {
	        }

	    }
	    
	    private class CommunicationThread extends Thread {
	    	
	    	private Socket socket;
	    	
	    	public CommunicationThread(Socket socket) {
	    		this.socket = socket;
	    	}
	    	
	    	@Override
	    	public void run() {
	    		try {
		    		Log.v(Constants.TAG, "Connection opened with " + socket.getInetAddress() + ":" + socket.getLocalPort());
		    		BufferedReader bufferedReader = Utilities.getReader(socket);
		            PrintWriter printWriter = Utilities.getWriter(socket);
		            
		            String word = bufferedReader.readLine();
                    
                    Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(Constants.WEB_SERVICE_ADDRESS);
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair(Constants.QUERY_ATTRIBUTE, word));
                    UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                    httpPost.setEntity(urlEncodedFormEntity);
                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    String pageSourceCode = httpClient.execute(httpPost, responseHandler);
                    
                    /*
                    if (pageSourceCode != null) {
                        Document document = Jsoup.parse(pageSourceCode);
                        Element element = document.child(0);
                        Elements scripts = element.getElementsByTag(Constants.SCRIPT_TAG);
                        for (Element script : scripts) {
                            String scriptData = script.data();
                        	Log.v(Constants.TAG, scriptData);
                        }
                    }
                    */
                    
                    String result = "";
                    
                    if (pageSourceCode != null) {
                        Document document = Jsoup.parse(pageSourceCode);
                        Element element = document.child(0);
                        Elements scripts = element.getElementsByTag(Constants.SCRIPT_TAG);
                        for (Element script : scripts) {

                            String scriptData = script.data();
                            result += " " + scriptData;
                            /*
                            if (scriptData.contains(Constants.SEARCH_KEY)) {
                                int position = scriptData.indexOf(Constants.SEARCH_KEY) + Constants.SEARCH_KEY.length();
                                scriptData = scriptData.substring(position);

                                JSONObject content = new JSONObject(scriptData);

                                JSONObject currentObservation = content.getJSONObject(Constants.CURRENT_OBSERVATION);
                                String temperature = currentObservation.getString(Constants.TEMPERATURE);
                                String windSpeed = currentObservation.getString(Constants.WIND_SPEED);
                                String condition = currentObservation.getString(Constants.CONDITION);
                                String pressure = currentObservation.getString(Constants.PRESSURE);
                                String humidity = currentObservation.getString(Constants.HUMIDITY);

                                break;
                            }
                            */
                        }
                    } else {
                        Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    }
                    
                    //String result = pageSourceCode;
                    //String result = "sss";
                    printWriter.println(result);
                    printWriter.flush();
                    
		            socket.close();
		            Log.v(Constants.TAG, "Connection closed");
	    		} catch (IOException ioException) {
	    			Log.d(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
	    			if (Constants.DEBUG) {
	    				ioException.printStackTrace();
	    			}
	    		}
	    	}
	    	
	    }
	    
	    private ClientThread clientThread = null;
	    private class ClientThread extends Thread {

	        private String address;
	        private int port;
	        private String city;
	        private String informationType;
	        private TextView weatherForecastTextView;

	        private Socket socket;

	        public ClientThread(
	                String address,
	                int port,
	                String city,
	                TextView weatherForecastTextView) {
	            this.address = address;
	            this.port = port;
	            this.city = city;
	            this.weatherForecastTextView = weatherForecastTextView;
	        }

	        @Override
	        public void run() {
	            try {
	                socket = new Socket(address, port);
	                if (socket == null) {
	                    Log.e(Constants.TAG, "[CLIENT THREAD] Could not create socket!");
	                }

	                BufferedReader bufferedReader = Utilities.getReader(socket);
	                PrintWriter printWriter = Utilities.getWriter(socket);
	                if (bufferedReader != null && printWriter != null) {
	                    printWriter.println(city);
	                    printWriter.flush();
	                    printWriter.println(informationType);
	                    printWriter.flush();
	                    String weatherInformation;
	                    while ((weatherInformation = bufferedReader.readLine()) != null) {
	                        final String finalizedWeatherInformation = weatherInformation;
	                        weatherForecastTextView.post(new Runnable() {
	                            @Override
	                            public void run() {
	                                weatherForecastTextView.append(finalizedWeatherInformation + "\n");
	                            }
	                        });
	                    }
	                } else {
	                    Log.e(Constants.TAG, "[CLIENT THREAD] BufferedReader / PrintWriter are null!");
	                }
	                socket.close();
	            } catch (IOException ioException) {
	                Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
	                if (Constants.DEBUG) {
	                    ioException.printStackTrace();
	                }
	            }
	        }

	    }
	    
	    private ServerThread serverThread;
	    public class ServerThread extends Thread {

	        private int port = 0;
	        private ServerSocket serverSocket = null;

	        //private HashMap<String, WeatherForecastInformation> data = null;

	        public ServerThread(int port) {
	            this.port = port;
	            try {
	                this.serverSocket = new ServerSocket(port);
	            } catch (IOException ioException) {
	                Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
	                if (Constants.DEBUG) {
	                    ioException.printStackTrace();
	                }
	            }
	            //this.data = new HashMap<String, WeatherForecastInformation>();
	        }
	        
	        @Override
	        public void run() {
	            try {
	                while (!Thread.currentThread().isInterrupted()) {
	                    Log.i(Constants.TAG, "[SERVER] Waiting for a connection...");
	                    Socket socket = serverSocket.accept();
	                    Log.i(Constants.TAG, "[SERVER] A connection request was received from " + socket.getInetAddress() + ":" + socket.getLocalPort());
	                    CommunicationThread communicationThread = new CommunicationThread(socket);
	                    communicationThread.start();
	                }
	            } catch (ClientProtocolException clientProtocolException) {
	                Log.e(Constants.TAG, "An exception has occurred: " + clientProtocolException.getMessage());
	                if (Constants.DEBUG) {
	                    clientProtocolException.printStackTrace();
	                }
	            } catch (IOException ioException) {
	                Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
	                if (Constants.DEBUG) {
	                    ioException.printStackTrace();
	                }
	            }
	        }

	        public void stopThread() {
	            if (serverSocket != null) {
	                interrupt();
	                try {
	                    serverSocket.close();
	                } catch (IOException ioException) {
	                    Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
	                    if (Constants.DEBUG) {
	                        ioException.printStackTrace();
	                    }
	                }
	            }
	        }

	    }

	    private ButtonClickListener1 connectButtonClickListener = new ButtonClickListener1();
	    private class ButtonClickListener1 implements Button.OnClickListener {

	        @Override
	        public void onClick(View view) {
	            String serverPort = serverPortEditText.getText().toString();
	            if (serverPort == null || serverPort.isEmpty()) {
	                Toast.makeText(
	                        getApplicationContext(),
	                        "Server port should be filled!",
	                        Toast.LENGTH_SHORT
	                ).show();
	                return;
	            }

	            serverThread = new ServerThread(Integer.parseInt(serverPort));
	            serverThread.start();
	            
                Log.v(Constants.TAG, "Starting server...");

	        }
	    }
	    
	    private ButtonClickListener2 getButtonClickListener = new ButtonClickListener2();
	    private class ButtonClickListener2 implements Button.OnClickListener {

	        @Override
	        public void onClick(View view) {
	        	String clientAddress = clientAddressEditText.getText().toString();
	            String clientPort    = clientPortEditText.getText().toString();
	            if (clientAddress == null || clientAddress.isEmpty() ||
	                    clientPort == null || clientPort.isEmpty()) {
	                Toast.makeText(
	                        getApplicationContext(),
	                        "Client connection parameters should be filled!",
	                        Toast.LENGTH_SHORT
	                ).show();
	                return;
	            }

	            String city = cityEditText.getText().toString();
	            
	            Log.v(Constants.TAG, city + "...");
	            clientThread = new ClientThread(
	                    clientAddress,
	                    Integer.parseInt(clientPort),
	                    city,
	                    clientTextView);
	            clientThread.start();

	        }
	    }
	    
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_practical_test02_main);

	        clientTextView = (TextView)findViewById(R.id.textView3);
	        //serverTextEditText.addTextChangedListener(serverTextContentWatcher);
	        
	        serverPortEditText = (EditText)findViewById(R.id.editText1);
	        
	        connectButton = (Button)findViewById(R.id.button1);
	        connectButton.setOnClickListener(connectButtonClickListener);

	        clientAddressEditText = (EditText)findViewById(R.id.editText2);
	        clientPortEditText = (EditText)findViewById(R.id.editText3);
	        cityEditText = (EditText)findViewById(R.id.editText4);
	        
	        getWeatherForecastButton = (Button)findViewById(R.id.button2);
	        getWeatherForecastButton.setOnClickListener(getButtonClickListener);
		}
		
	    @Override
	    protected void onDestroy() {
	        super.onDestroy();
	        if (serverThread != null) {
	            serverThread.stopThread();
	        }
	    }

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.practical_test02_main, menu);
			return true;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// Handle action bar item clicks here. The action bar will
			// automatically handle clicks on the Home/Up button, so long
			// as you specify a parent activity in AndroidManifest.xml.
			int id = item.getItemId();
			if (id == R.id.action_settings) {
				return true;
			}
			return super.onOptionsItemSelected(item);
		}

}
