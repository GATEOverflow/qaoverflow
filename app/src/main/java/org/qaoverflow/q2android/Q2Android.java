
package org.qaoverflow.q2android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.qaoverflow.q2android.authentication.AccountGeneral;
import org.qaoverflow.q2android.interfaces.Q2AAnimations;
import org.qaoverflow.q2android.interfaces.Q2ARequest;
import org.qaoverflow.q2android.interfaces.Q2AStrings;
import org.qaoverflow.q2android.interfaces.Q2AWebsite;
import org.qaoverflow.q2android.interfaces.StreamListAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Q2Android extends SherlockListActivity {
	
	protected String TAG = "Q2Android";

	public static String versionName = "0.7";
	
	private static SharedPreferences prefs;
	private static Q2Android activity;
	private StreamListAdapter adapter;
	private String website;

	private ListView listView;
	private ScrollView questionPane;
	private LinearLayout questionPad;

	private TextView questionTitle;
	private TextView questionMeta;
	private TextView questionContent;
	private TextView questionVotes;
	private ImageView questionAvatar;

	private Button questionVoteUp;
	private Button questionVoteDown;

	private HorizontalScrollView questionButtons;

	private ImageButton questionFavorite;

	private LinearLayout answerContainer;

	protected static int currentScope;

	public static int NOTIFY_ID = 0;

	private Intent intent;

	private MenuItem refreshItem;
	private boolean refreshing;

	protected ArrayList<CharSequence> notificationStrings;
	protected ArrayList<String> notificationLinks;

	protected HashMap<String,Boolean> adminRights = new HashMap<String,Boolean>();

	protected boolean submitting;

	private int lastScope;

	private ActionBar actionBar;

	private int currentPage = 0;
	private HashMap<?,?> currentQuestion;
	private int currentQuestionId;
	protected Object[] currentQuestionList;
	private int currentPosition = 0;

	private int selChildId;
	private Boolean isByUser;
	private Boolean isSelectable;
	
	private LinearLayout commentContainer;

	private SlidingMenu slideMenu;

	private ListView filters;

	private LayoutInflater layoutInflater;

    private WebView answerWebView;

    private TextView newText1;
    private TextView newText2;


	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(org.qaoverflow.q2android.R.layout.main);
		
		layoutInflater = getLayoutInflater();
		mAccountManager = AccountManager.get(this);
		actionBar = getSupportActionBar();
		
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		questionPane = (ScrollView)findViewById(org.qaoverflow.q2android.R.id.q_pane);

		intent = this.getIntent();
		
    	listView = getListView();
		questionPad = (LinearLayout) findViewById(org.qaoverflow.q2android.R.id.q_pad);

		questionTitle = (TextView) findViewById(org.qaoverflow.q2android.R.id.qtitle);
		questionAvatar = (ImageView) findViewById(org.qaoverflow.q2android.R.id.qavatar);
		questionMeta = (TextView) findViewById(org.qaoverflow.q2android.R.id.qmeta);
		//questionContent = (TextView) findViewById(R.id.qcontent);
		questionVotes = (TextView) findViewById(org.qaoverflow.q2android.R.id.qnetvotes);

		questionVoteUp = (Button) findViewById(org.qaoverflow.q2android.R.id.qvote_up_button);
		questionVoteDown = (Button) findViewById(org.qaoverflow.q2android.R.id.qvote_down_button);
		
		questionButtons = (HorizontalScrollView) findViewById(org.qaoverflow.q2android.R.id.qbuttons);

		questionFavorite = (ImageButton) findViewById(org.qaoverflow.q2android.R.id.starrer);
		commentContainer = (LinearLayout) findViewById(org.qaoverflow.q2android.R.id.qcomments);
		answerContainer = (LinearLayout) findViewById(org.qaoverflow.q2android.R.id.acontainer);

        slideMenu = new SlidingMenu(this);
        slideMenu.setMode(SlidingMenu.LEFT);
        slideMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        //menu.setShadowWidthRes(0);
        //menu.setShadowDrawable(R.drawable.shadow);
        slideMenu.setBehindWidthRes(org.qaoverflow.q2android.R.dimen.slide_width);
        slideMenu.setFadeDegree(0.35f);
        slideMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        slideMenu.setMenu(org.qaoverflow.q2android.R.layout.slide);

        filters = (ListView) slideMenu.getMenu().findViewById(org.qaoverflow.q2android.R.id.filters);


        answerWebView = (WebView) findViewById(org.qaoverflow.q2android.R.id.answerWebView);
        answerWebView.getSettings().setJavaScriptEnabled(true);
        //initiateAnswerWebView();

        answerWebView.loadUrl("file:///android_asset/MathJax/index.html");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
             android.R.layout.simple_list_item_1,
             Q2AStrings.getFilterDisplayStrings(this));
        filters.setAdapter(adapter);
        filters.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				currentPage = 0;
				currentScope = Q2AStrings.STREAMS[arg2];
				getQuestions(null, currentScope);
				slideMenu.showContent(true);
			}
        	
        });
        
    	currentScope = Q2AStrings.UPDATED;
    	lastScope = currentScope;
    	
    	registerForContextMenu(listView);
	    listView.setTextFilterEnabled(true);
	    
	    activity = this;
    	website = Q2AWebsite.getWebsite(this);

    	adjustLayout();
    	
    	if(prefs.getBoolean("auto_update", true) && !getIntent().hasExtra("notification"))
    		refreshStream(currentScope);
    	else if(Q2AWebsite.getWebsite(this) == null || prefs.getString("username", null) == null || prefs.getString("password", null) == null) {
			Intent i = new Intent(this, Q2ALoginActivity.class);
			startActivityForResult(i, RESULT_LOGIN);
		}
	}

	@Override
	public void onResume(){
		super.onResume();


    	activity = this;
    	String newWebsite = Q2AWebsite.getWebsite(this);
    	adjustLayout();

    	if(intent.hasExtra(Intent.EXTRA_TEXT)) {
    		Log.i("Q2Android","Got text: "+intent.getStringExtra(Intent.EXTRA_TEXT));
    		intent.removeExtra(Intent.EXTRA_TEXT);
    	}
    	
    	// if website changed
    	
    	if(website != null && !website.equals(newWebsite)) {
    		website = newWebsite;
    	}

	}

	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(org.qaoverflow.q2android.R.menu.menu_main, menu);
	    
	    refreshItem = menu.findItem(org.qaoverflow.q2android.R.id.menuStream);
	    if(refreshing)
	    	showRefresh();
	    return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		super.onOptionsItemSelected(item);
		
		Intent intent;
		switch (item.getItemId()) {
	        case android.R.id.home:
	        	if(!isQuestion || isLandscape) {
	        		slideMenu.toggle(true);
	        		isQuestion = false;
	        	}
	        	else {
	        		isQuestion = false;
	        		adjustLayout();
	        	}
	            return true;

			case (int) org.qaoverflow.q2android.R.id.menuStream:
		    	if(Q2AWebsite.getWebsite(this) == null) {
					Toast.makeText(this, getString(org.qaoverflow.q2android.R.string.noWebsite),
							Toast.LENGTH_LONG).show();
					return true;
		    	}
				refreshStream(currentScope);
				break;
			case (int) org.qaoverflow.q2android.R.id.menuNew:
				final LinearLayout questionLayout = (LinearLayout) layoutInflater.inflate(org.qaoverflow.q2android.R.layout.question_new, null);
				final EditText title = (EditText) questionLayout.findViewById(org.qaoverflow.q2android.R.id.title);
				final EditText content = (EditText) questionLayout.findViewById(org.qaoverflow.q2android.R.id.content);
				final EditText tags = (EditText) questionLayout.findViewById(org.qaoverflow.q2android.R.id.tags);
				new AlertDialog.Builder(activity)
			    .setTitle(org.qaoverflow.q2android.R.string.new_question)
			    .setView(questionLayout)
			    .setPositiveButton(org.qaoverflow.q2android.R.string.submit, new DialogInterface.OnClickListener() {
	
					public void onClick(DialogInterface dialog, int whichButton) {
						HashMap<String,Object> data = new HashMap<String,Object>();
						HashMap<String,Object> info = new HashMap<String,Object>();

						info.put("type", "Q");
						info.put("title", title.getText().toString());
						info.put("content", edit.getText().toString());
						info.put("tags", tags.getText().toString());
						info.put("format", prefs.getString("editor_type", ""));

						data.put("action_data", info);
						data.put("action","post");
						getQuestions(data,currentScope);

						hideKeyboard(title);
		        		isQuestion = false;
		        		adjustLayout();						

			        }
			    }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
						hideKeyboard(questionLayout);
			        }
			    }).show();

                button2 = (Button) questionLayout.findViewById(org.qaoverflow.q2android.R.id.button2);

                button2.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickBtn(2);
                    }
                });

                button3 = (Button) questionLayout.findViewById(org.qaoverflow.q2android.R.id.button3);

                button3.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickBtn(3);
                    }
                });

                button4 = (Button) questionLayout.findViewById(org.qaoverflow.q2android.R.id.button4);

                button4.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickBtn(4);
                    }
                });
                edit = (EditText) questionLayout.findViewById(org.qaoverflow.q2android.R.id.edit);

                newText1 = (TextView) questionLayout.findViewById(org.qaoverflow.q2android.R.id.newText1);
                newText2 = (TextView) questionLayout.findViewById(org.qaoverflow.q2android.R.id.newText2);

                w = (WebView) questionLayout.findViewById(org.qaoverflow.q2android.R.id.webview);
                w.getSettings().setJavaScriptEnabled(true);
                w.setBackgroundColor(Color.TRANSPARENT);
                w.addJavascriptInterface(new Object() { public void clipMML(String s) {
                    WebView ww = (WebView) findViewById(org.qaoverflow.q2android.R.id.webview);
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(s);
                    Toast.makeText(getApplicationContext(),"MathML copied to clipboard",Toast.LENGTH_SHORT).show();
                }}, "injectedObject");
                initiateWebView("");
				break;
			case (int) org.qaoverflow.q2android.R.id.menuLogin:
				showAccountPicker(AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, false);
				break;
			case (int) org.qaoverflow.q2android.R.id.menuPrefs:
				intent = new Intent(this, Q2ASettingsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;
			case (int) org.qaoverflow.q2android.R.id.menuHelp:
				intent = new Intent(this, Q2AHelpActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;

			default:
				return false;
	    }
		return true;
	}	

	public static final int RESULT_USER = 0;
	public static final int RESULT_LOGIN = 1;
	
	protected void  onActivityResult (int requestCode, int resultCode, Intent  data) {
		
		if(requestCode == RESULT_USER && resultCode != Activity.RESULT_OK)		
			refreshStream(currentScope);
		else if(requestCode == RESULT_LOGIN && resultCode == Activity.RESULT_OK) {
			String ws = data.getStringExtra("website");
			String un = data.getStringExtra("username");
			String pw = data.getStringExtra("password");

			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("website", ws);
			editor.putString("username", un);
			editor.putString("password", pw);
			editor.commit();
			refreshStream(currentScope);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.i(TAG,"clicked stream item");
		currentPosition  = position;
		Object obj = getListView().getItemAtPosition(position);
		if(!(obj instanceof HashMap)) {
			if(obj instanceof String && obj.equals("<more>")) {
				currentPage++;
				getQuestions(null, currentScope);
			}
			else if(obj instanceof String && obj.equals("<less>")) {
				currentPage--;
				getQuestions(null, currentScope);
			}
			return;
		}
		currentQuestion = (HashMap<?, ?>) obj;
		isQuestion = true;
		adjustLayout();
		questionPane.scrollTo(0, 0);
		loadQuestion();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		android.view.MenuInflater inflater = getMenuInflater();
       	
		inflater.inflate(org.qaoverflow.q2android.R.menu.stream_longclick, menu);
        menu.setHeaderTitle(getString(org.qaoverflow.q2android.R.string.q_options));
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    int index = info.position;
	    final HashMap<?,?> entryMap = (HashMap<?, ?>) getListView().getItemAtPosition(index);
		Intent i;
	    
	    String link;
		link = (String)entryMap.get("url");
		link = link.replaceFirst("^\\./", Q2AWebsite.getWebsite(this));

	    Uri url;
		final EditText input;
		HashMap<String, Object> data;
		switch (item.getItemId()) {
			case org.qaoverflow.q2android.R.id.view:
				url = Uri.parse(link);
				i = new Intent(Intent.ACTION_VIEW, url);
				activity.startActivity(i);
				return true;
			case org.qaoverflow.q2android.R.id.share_link:
				i = new Intent(Intent.ACTION_SEND);
				i.putExtra(Intent.EXTRA_TEXT, link);
				i.setType("text/plain");
				startActivity(Intent.createChooser(i, getString(org.qaoverflow.q2android.R.string.share_via)));
				return true;
			default:
				break;
		}
		
		return super.onContextItemSelected(item);
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        adjustLayout();
    }

	

	protected void refreshStream(int which) {
		if(refreshing)
			return;

		if(Q2AWebsite.getWebsite(this) == null || prefs.getString("username", null) == null || prefs.getString("password", null) == null) {
			showAccountPicker(AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, false);
			return;
		}
		
		Log.d(TAG,"getting for currentScope of "+which);
		lastScope = which;
		
		getQuestions(null, which);

	}
	
	public void getQuestions(HashMap<String, Object> data, int ascope) {
		
		if(refreshing)
			return;
		
		lastScope = ascope;

		Log.i(TAG ,"getting questions for "+Q2AStrings.FILTERS[ascope]);
		
		if(data == null)
			data = new HashMap<String, Object>();
		
		data.put("sort", Q2AStrings.FILTERS[ascope]);
		data.put("meta_data", "true");
		data.put("more", "true");
		data.put("full", "true");
		
		int max = Integer.parseInt(prefs.getString("stream_max", "20"));
		
		data.put("start", currentPage*max);
		data.put("size", max);
		
		Q2ARequest stream = new Q2ARequest(activity, mHandler, "q2a.getQuestions", data, MSG_QUESTIONS);
		stream.execute();
		showRefresh();
	}

	public void getQuestion(HashMap<String, Object> data) {
		
		if(refreshing)
			return;
		
		Log.i(TAG ,"getting single question");
		
		if(data == null)
			data = new HashMap<String, Object>();
		
		data.put("meta_data", "true");
		data.put("full", "true");
		
		Q2ARequest stream = new Q2ARequest(activity, mHandler, "q2a.getQuestion", data, MSG_QUESTION);
		stream.execute();
		showRefresh();
	}

	
	public static final int MSG_QUESTIONS = 0;
	public static final int MSG_QUESTION = 1;
	public static final int MSG_VOTE = 2;
	public static final int MSG_SCOPE = 3;

	private Handler mHandler = new Handler() {
		

		@Override
        public void handleMessage(Message msg) {
			Log.i(TAG ,"got message");
			completeRefresh();

			HashMap<?, ?> map;
			Object obj;
			Object[] list;
			
			String toast = null;
			
			Integer position;
			switch(msg.what) {
				case MSG_QUESTIONS:
					if(!(msg.obj instanceof HashMap)) {
						Log.w(TAG,"message not a map: "+msg.obj.getClass());
						if(msg.obj instanceof String)
							toast = (String) msg.obj;
						setEmptyList();
						break;
					}
					
					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("data");
					
					if(!(obj instanceof Object[])) {
						Log.w(TAG,"data not an Object[]: "+obj.getClass());
						if(obj instanceof String)
							toast = (String) obj;
						setEmptyList();
						break;
					}
					
					list = (Object[]) obj;
					
					if(list.length == 0) {
						Log.w(TAG,"data empty");
						if(map.get("message") instanceof String)
							toast = (String) map.get("message");
						setEmptyList();
						questionPad.setVisibility(View.GONE);
						break;
					}
					
					if(map.containsKey("user_data") && map.get("user_data") instanceof HashMap){
						Log.i(TAG ,"got user data");
						Map<?,?> user = (HashMap<?, ?>) map.get("user_data");

						if(user.get("level") instanceof String) {
							
						}
					}
					
					currentQuestionList = list;
					
					adapter = new StreamListAdapter(activity,list);
					setListAdapter(adapter);

					if(map.containsKey("acted"))
						position = (Integer)map.get("acted");
					else if(!isLandscape && !isQuestion)
						break;
					else
						position = 0;
						
					
					Object cobj = getListView().getItemAtPosition(position);
					if(!(cobj instanceof HashMap))
						break;
					currentQuestion = (HashMap<?, ?>) cobj;
					isQuestion = true;
					adjustLayout();
					loadQuestion();
					
					toast = getString(org.qaoverflow.q2android.R.string.updated);
					
					currentScope = lastScope;
					actionBar.setTitle(getString(org.qaoverflow.q2android.R.string.app_name)+" - "+getString(Q2AStrings.STRINGS[currentScope]));
					break;
				case MSG_QUESTION:
					if(!(msg.obj instanceof HashMap)) {
						Log.w(TAG,"message not a map: "+msg.obj.getClass());
						if(msg.obj instanceof String)
							toast = (String) msg.obj;
						break;
					}
					
					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("data");
					
					if(!(obj instanceof HashMap)) {
						Log.w(TAG,"data not a HashMap: "+obj.getClass());
						if(obj instanceof String)
							toast = (String) obj;
						break;
					}
					
					HashMap<?, ?> aQuestion = (HashMap<?, ?>) obj;
					
					if(map.containsKey("user_data") && map.get("user_data") instanceof HashMap){
						Log.i(TAG ,"got user data");
						Map<?,?> user = (HashMap<?, ?>) map.get("user_data");

						if(user.get("level") instanceof String) {
							
						}
					}
					
					currentQuestionList[currentPosition] = aQuestion;
					currentQuestion = aQuestion;
					isQuestion = true;
					adjustLayout();
					loadQuestion();
					
					//toast = getString(R.string.updated);
					
					break;
				case MSG_SCOPE:
					if((msg.obj instanceof String)) { 
						if(((String) msg.obj).startsWith("http")) {
							Uri url = Uri.parse((String) msg.obj);
							Intent i = new Intent(Intent.ACTION_VIEW, url);
							activity.startActivity(i);							
						}
					}
					else
						refreshStream(msg.arg1);

					return;
				default: 
					if(msg.obj instanceof String)
						toast = (String) msg.obj;
					else
						toast = getString(org.qaoverflow.q2android.R.string.error);
					break;
			}
			submitting = false;
			
			if(toast != null)
				Toast.makeText(activity, (CharSequence) toast,
					Toast.LENGTH_LONG).show();

				
		}
    };

	private boolean isQuestion;
	private boolean isLandscape = false;

    private AccountManager mAccountManager;
    
	private void adjustLayout() {
		DisplayMetrics metrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(metrics);

    	int px = getResources().getDimensionPixelSize(org.qaoverflow.q2android.R.dimen.stream_width);
    	
    	int width = metrics.widthPixels; 	
    	//boolean land = width > 600;
        boolean land = false;

        if(land) {
    		questionPane.setVisibility(View.VISIBLE);
    		listView.setLayoutParams(new LayoutParams(px, LayoutParams.MATCH_PARENT));
    		listView.setVisibility(View.VISIBLE);
    		isQuestion = false;
    	}
    	else if(isQuestion){
    		questionPane.setVisibility(View.VISIBLE);
    		listView.setVisibility(View.GONE);
    	}
    	else {
    		questionPane.setVisibility(View.GONE);
    		listView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    		listView.setVisibility(View.VISIBLE);
    	}
    	isLandscape  = land;
	}

	protected void doSlideToggle(View view) {
		if(view.getVisibility() == View.GONE)
			doSlideDown(view);
		else
			doSlideUp(view);
	}

	private void setEmptyList() {
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1));
	}

	public void doSlideDown(View view){
		if(view.getVisibility() == View.VISIBLE || view.getAnimation() != null)
			return;
		view.setVisibility(View.VISIBLE);
		Animation slideDown = Q2AAnimations.slideDown();
		view.startAnimation(slideDown);
	}

	public void doSlideUp(View view){
		if(view.getVisibility() == View.GONE || view.getAnimation() != null)
			return;

		Animation slideUp = Q2AAnimations.slideUp(view); 
		view.startAnimation(slideUp);
	}

	public void showRefresh() {
		if(refreshItem == null) {
			Log.i(TAG,"not ready to show refresh");
			refreshing = true;
			return;
		}
		//Log.i(TAG,"showing refresh");
		
		/* Attach a rotating ImageView to the refresh item as an ActionView */
		ImageView iv = (ImageView) layoutInflater.inflate(org.qaoverflow.q2android.R.layout.rotate, null);
		
		Animation rotation = AnimationUtils.loadAnimation(this, org.qaoverflow.q2android.R.animator.rotate);
		rotation.setRepeatCount(Animation.INFINITE);
		iv.startAnimation(rotation);
		refreshItem.setActionView(iv);
	}
	public void completeRefresh() {
    	refreshing = false;
		if(refreshItem == null || refreshItem.getActionView() == null)
			return;	
		
		refreshItem.getActionView().clearAnimation();
		refreshItem.setActionView(null);
	}

	
	protected void loadQuestion() {
		Log.d(TAG,"Loading question");
		try {
			
			final HashMap<?,?> rawMap = (HashMap<?, ?>) currentQuestion.get("raw");
			final int questionId = Integer.parseInt((String) rawMap.get("postid")); 
			currentQuestionId = questionId;
			
			Object selchild = rawMap.get("selchildid");
			if(selchild instanceof String && ((String)selchild).length() > 0)
				selChildId = Integer.parseInt((String) selchild);
			else selChildId = 0;

			isByUser = (Boolean) rawMap.get("isbyuser");
			isSelectable = (Boolean) rawMap.get("aselectable");

			
    		String title = (String) rawMap.get("title");
            final String cont = (String) rawMap.get("content");
            Log.d("ansverContentTrue", cont);
			Spanned content = Q2AStrings.getEntryContent((String) currentQuestion.get("content"));

			String meta = Q2AStrings.getMetaString(activity, currentQuestion, false);
			Spanned metas = Html.fromHtml(metaLinkToServer(meta));

        	String img = (String)currentQuestion.get("avatar");

    		String votes = (String) rawMap.get("netvotes");
        	if(!votes.startsWith("-") && !votes.equals("0"))
        		votes = "+"+votes;

    		questionVoteDown.setVisibility(View.VISIBLE);
    		questionVoteUp.setVisibility(View.VISIBLE);
        	
        	Boolean voted = true;
			String uservote = (String) rawMap.get("uservote");
        	if(uservote.equals("1")) {
        		questionVoteUp.setSelected(true);
        		questionVoteDown.setVisibility(View.GONE);
        	}
        	else if(uservote.equals("-1")) {
        		questionVoteDown.setSelected(true);
        		questionVoteUp.setVisibility(View.GONE);
        	}
        	else if(currentQuestion.get("vote_state").equals("disabled")) {
        		questionVoteUp.setSelected(false);
        		questionVoteDown.setSelected(false);
        		questionVoteUp.setVisibility(View.GONE);
        		questionVoteDown.setVisibility(View.GONE);
        	}
        	else {
        		voted = false;
        	}       	
		// voting

        	final int upvote = voted?0:1;
        	final int downvote = voted?0:-1;

        	
			questionVoteUp.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					HashMap<String,Object> data = new HashMap<String,Object>();

					HashMap<String,Object> info = new HashMap<String,Object>();
					info.put("vote", upvote);
					info.put("type", "Q");
					
					data.put("action_data", info);
					data.put("postid", questionId);
					data.put("action","vote");
					data.put("action_id", questionId);
					getQuestion(data);
					
				}
				
			});

			questionVoteDown.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					HashMap<String,Object> data = new HashMap<String,Object>();
					HashMap<String,Object> info = new HashMap<String,Object>();
					info.put("vote", downvote);
					info.put("type", "Q");
					
					data.put("action_data", info);
					data.put("postid", questionId);
					data.put("action","vote");
					data.put("action_id", questionId);
					getQuestion(data);
					
				}
				
			});

		// action buttons
			
			questionButtons.removeAllViews();
			questionButtons.addView(getPostButtons(currentQuestion));
			
		// favorite
			
			Object favorite = currentQuestion.get("favorite"); 

			if(favorite instanceof String && ((String)favorite).equals("1")) { // is favorite
				questionFavorite.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						HashMap<String,Object> data = new HashMap<String,Object>();
						HashMap<String,Object> info = new HashMap<String,Object>();
						
						info.put("type", "Q");
						
						data.put("action_data", info);
						data.put("postid", questionId);
						data.put("action","favorite");
						data.put("action_id", questionId);
						getQuestion(data);
						
					}
					
				});
				questionFavorite.setSelected(true);
			}
			else {
				questionFavorite.setOnClickListener(new OnClickListener() { // is not favorite

					@Override
					public void onClick(View v) {
						HashMap<String,Object> data = new HashMap<String,Object>();
						HashMap<String,Object> info = new HashMap<String,Object>();
						
						info.put("favorite", "true");
						info.put("type", "Q");
						
						data.put("action_data", info);
						data.put("postid", questionId);
						data.put("action","favorite");
						data.put("action_id", questionId);
						getQuestion(data);
					}
					
				});
				questionFavorite.setSelected(false);
			}

            //answerWebView.loadUrl("file:///android_asset/MathJax/index.html");
            //answerWebView.evaluateJavascript("javascript:document.getElementById('math').innerHTML='';",null);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                answerWebView.evaluateJavascript("javascript:document.getElementById('math').innerHTML='"+doubleEscapeTeX(cont)+"';",null);
                answerWebView.evaluateJavascript("javascript:MathJax.Hub.Queue(['Typeset',MathJax.Hub]);", null);
            } else {
                answerWebView.loadUrl("javascript:document.getElementById('math').innerHTML='"+doubleEscapeTeX(cont)+"';");
                answerWebView.loadUrl("javascript:MathJax.Hub.Queue(['Typeset',MathJax.Hub]);");
            }



			questionTitle.setText(title);
			//questionContent.setText(content);

			questionMeta.setText(metas);
			questionVotes.setText(votes);
			
        	UrlImageViewHelper.setUrlDrawable(questionAvatar, img);

        	answerContainer.removeAllViews();
        	commentContainer.removeAllViews();

        	if(currentQuestion.get("answers") instanceof Object[])
        		addAnswers((Object[])currentQuestion.get("answers"), questionId);

        	if(currentQuestion.get("comments") instanceof Object[]) {
				addComments((Object[]) currentQuestion.get("comments"), commentContainer);
        	}
        	
			questionPad.setVisibility(View.VISIBLE);
		}
		catch(Exception e) {
			e.printStackTrace();

		}
		isQuestion = true;
	}


	private void addAnswers(Object[] answers, final int questionId) {
		for(Object obj : answers) {
			if(obj instanceof HashMap) {
				HashMap<?,?> answer = (HashMap<?, ?>) obj;
				
				// Inflate the views from XML
				LinearLayout answerView = (LinearLayout) layoutInflater.inflate(org.qaoverflow.q2android.R.layout.answer, null);

				TextView metaView = (TextView) answerView.findViewById(org.qaoverflow.q2android.R.id.meta);
				TextView contentView = (TextView) answerView.findViewById(org.qaoverflow.q2android.R.id.content);



				TextView votesView = (TextView) answerView.findViewById(org.qaoverflow.q2android.R.id.netvotes);
				Button voteUp = (Button) answerView.findViewById(org.qaoverflow.q2android.R.id.vote_up_button);
				Button voteDown = (Button) answerView.findViewById(org.qaoverflow.q2android.R.id.vote_down_button);
				ImageButton answerSelect = (ImageButton) answerView.findViewById(org.qaoverflow.q2android.R.id.selector);

				ImageView avatarView = (ImageView) answerView.findViewById(org.qaoverflow.q2android.R.id.avatar);
				
				HorizontalScrollView buttonsView = (HorizontalScrollView) answerView.findViewById(org.qaoverflow.q2android.R.id.buttons);

                final WebView answeredWebView = (WebView) answerView.findViewById(org.qaoverflow.q2android.R.id.answerWebView);
                answeredWebView.getSettings().setJavaScriptEnabled(true);



				
				try {
					final HashMap<?,?> rawMap = (HashMap<?, ?>) answer.get("raw");
					
					final int answerId = Integer.parseInt((String) rawMap.get("postid")); 
					
					Integer vint = (Integer) answer.get("netvotes_raw");
		        	
					String votes = Integer.toString(vint);
					if(vint > 0)
		        		votes = "+"+votes;

		        	voteDown.setVisibility(View.VISIBLE);
		    		voteUp.setVisibility(View.VISIBLE);
		        	
		        	Boolean voted = true;
					String uservote = (String) rawMap.get("uservote");
		        	if(uservote.equals("1")) {
		        		voteUp.setSelected(true);
		        		voteDown.setVisibility(View.GONE);
		        	}
		        	else if(uservote.equals("-1")) {
		        		voteDown.setSelected(true);
		        		voteUp.setVisibility(View.GONE);
		        	}
		        	else if(answer.get("vote_state").equals("disabled")) {
		        		voteUp.setVisibility(View.GONE);
		        		voteDown.setVisibility(View.GONE);
		        	}
		        	else {
		        		voted = false;
		        	}
		        	
		        	final int upvote = voted?0:1;
		        	final int downvote = voted?0:-1;
		        	
					Spanned content = Q2AStrings.getEntryContent((String) answer.get("content"));

					String meta = Q2AStrings.getMetaString(activity, answer, false);
                    meta = metaLinkToServer(meta);
                    Log.d("META",meta);
					Spanned metas = Html.fromHtml(metaLinkToServer(meta));



					// clickables
					
					
					voteUp.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							HashMap<String,Object> data = new HashMap<String,Object>();
							HashMap<String,Object> info = new HashMap<String,Object>();
							info.put("vote", upvote);
							info.put("type", "A");
							
							data.put("action_data", info);
							data.put("postid", questionId);
							data.put("action","vote");
							data.put("action_id", (String)rawMap.get("postid"));
							getQuestion(data);
							
						}
						
					});

					voteDown.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							HashMap<String,Object> data = new HashMap<String,Object>();
							HashMap<String,Object> info = new HashMap<String,Object>();
							info.put("vote", downvote);
							info.put("type", "A");
							
							data.put("action_data", info);
							data.put("postid", questionId);
							data.put("action","vote");
							data.put("action_id", (String)rawMap.get("postid"));
							getQuestion(data);
							
						}
						
					});

					// buttons
					
					buttonsView.addView(getPostButtons(answer));
					
					
					// selector

					if(isSelectable) {
						if(answerId == selChildId) {
							answerView.setBackgroundColor(0xFFDDFFDD);
							answerSelect.setVisibility(View.VISIBLE);
							
							answerSelect.setSelected(true);
							
							answerSelect.setOnClickListener(new OnClickListener() {
	
								@Override
								public void onClick(View v) {
									HashMap<String,Object> data = new HashMap<String,Object>();
									HashMap<String,Object> info = new HashMap<String,Object>();
									info.put("question_id", questionId);
																	
									data.put("action_data", info);									
									data.put("postid", questionId);
									data.put("action","select");
									data.put("action_id", (String)rawMap.get("postid"));
									getQuestion(data);
								}
								
							});
						}
						else if (selChildId == 0) {
							answerSelect.setVisibility(View.VISIBLE);
							answerSelect.setOnClickListener(new OnClickListener() {
	
								@Override
								public void onClick(View v) {
									HashMap<String,Object> data = new HashMap<String,Object>();
									HashMap<String,Object> info = new HashMap<String,Object>();
									info.put("question_id", questionId);
									info.put("select", "true");
																	
									data.put("action_data", info);									
									data.put("postid", questionId);
									data.put("action","select");
									data.put("action_id", (String)rawMap.get("postid"));
									getQuestion(data);
								}
								
							});
							answerSelect.setSelected(false);
						}
					}
					else if(answerId == selChildId) {
						answerView.setBackgroundColor(0xFFDDFFDD);
						answerSelect.setVisibility(View.VISIBLE);
						answerSelect.setSelected(true);
						answerSelect.setEnabled(false);
					}

					
					String img = (String)answer.get("avatar");
					
					contentView.setText(content);

					metaView.setText(metas);
					votesView.setText(votes);
					
		        	UrlImageViewHelper.setUrlDrawable(avatarView, img);



		    		// get comments
		        	if(answer.get("comments") instanceof Object[]) {
						Object[] comments = (Object[]) answer.get("comments");
		        		if(comments.length > 0) {
							LinearLayout commentView = (LinearLayout) answerView.findViewById(org.qaoverflow.q2android.R.id.comments);
							addComments(comments, commentView);
		        		}
		        	}
                    answeredWebView.loadUrl("file:///android_asset/MathJax/index.html");
                    final String responce = (String) answer.get("content");
                    answeredWebView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                            Log.d("unnseeerrr",responce);
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                answeredWebView.evaluateJavascript("javascript:document.getElementById('math').innerHTML='"+doubleEscapeTeX(responce)+"';",null);
                                answeredWebView.evaluateJavascript("javascript:MathJax.Hub.Queue(['Typeset',MathJax.Hub]);", null);
                            } else {
                                answeredWebView.loadUrl("javascript:document.getElementById('math').innerHTML='"+doubleEscapeTeX(responce)+"';");
                                answeredWebView.loadUrl("javascript:MathJax.Hub.Queue(['Typeset',MathJax.Hub]);");
                            }


                        }
                    });


                    answerContainer.addView(answerView);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void addComments(Object[] comments, LinearLayout view) {
		
		for(Object obj : comments) {
			if(obj instanceof HashMap) {
				HashMap<?,?> comment = (HashMap<?, ?>) obj;
				LinearLayout commentView = (LinearLayout) layoutInflater.inflate(org.qaoverflow.q2android.R.layout.comment, null);
				TextView metaView = (TextView) commentView.findViewById(org.qaoverflow.q2android.R.id.meta);
				TextView contentView = (TextView) commentView.findViewById(org.qaoverflow.q2android.R.id.content);
				HorizontalScrollView buttonsView = (HorizontalScrollView) commentView.findViewById(org.qaoverflow.q2android.R.id.buttons);
				
				String meta = Q2AStrings.getMetaString(this, comment, false);
				Spanned metas = Html.fromHtml(metaLinkToServer(meta));
				
				Spanned content = Q2AStrings.getEntryContent((String) comment.get("content"));

				// buttons
				
				buttonsView.addView(getPostButtons(comment));
				
				// add text
				
	        	metaView.setText(metas);
	        	contentView.setText(content);
	        	view.addView(commentView);
			}
		}
	}
	
	private void hideKeyboard(View v) {
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		
	}

    private Button button2;
    private Button button3;
    private Button button4;

    private WebView w;
    private LinearLayout questionLayout;
    private EditText edit;

	private LinearLayout getPostButtons(HashMap<?,?> post) {
		boolean first = true;
		final HashMap<?,?> rawMap = (HashMap<?, ?>) post.get("raw");

		LinearLayout buttons = (LinearLayout) layoutInflater.inflate(org.qaoverflow.q2android.R.layout.buttons, null);
		
		if((Boolean)rawMap.get("editbutton")) {
			Button abutton = (Button) layoutInflater.inflate(org.qaoverflow.q2android.R.layout.button, null);
			abutton.setText(getString(org.qaoverflow.q2android.R.string.edit));

			abutton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View v) {
					if(rawMap.get("basetype").equals("Q")) {
						final LinearLayout questionLayout = (LinearLayout) layoutInflater.inflate(org.qaoverflow.q2android.R.layout.question_new, null);
						final EditText title = (EditText) questionLayout.findViewById(org.qaoverflow.q2android.R.id.title);

                        final EditText e = (EditText) questionLayout.findViewById(org.qaoverflow.q2android.R.id.edit);
                        e.setText((String)rawMap.get("content"));

                        final EditText tags = (EditText) questionLayout.findViewById(org.qaoverflow.q2android.R.id.tags);
						title.setText((String) rawMap.get("title"));
						//content.setText((String) rawMap.get("content"));

                        final WebView webview = (WebView) questionLayout.findViewById(org.qaoverflow.q2android.R.id.webview);
                        webview.getSettings().setJavaScriptEnabled(true);
                        webview.loadUrl("file:///android_asset/MathJax/index.html");

                        final Button button2 = (Button) questionLayout.findViewById(org.qaoverflow.q2android.R.id.button2);
                        button2.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                webview.evaluateJavascript("javascript:document.getElementById('math').innerHTML='" + doubleEscapeTeX(e.getText().toString()) + "';", null);
                                webview.loadUrl("javascript:MathJax.Hub.Queue(['Typeset',MathJax.Hub]);");
                            }
                        });

                        final Button button3 = (Button) questionLayout.findViewById(org.qaoverflow.q2android.R.id.button3);
                        button3.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                e.setText("");
                                webview.evaluateJavascript("javascript:document.getElementById('math').innerHTML='';", null);
                                webview.loadUrl("javascript:MathJax.Hub.Queue(['Typeset',MathJax.Hub]);");
                            }
                        });
                        Button button4 = (Button) questionLayout.findViewById(org.qaoverflow.q2android.R.id.button4);
                        button4.setVisibility(View.GONE);
                        
						tags.setText((String) rawMap.get("tags"));
						
						new AlertDialog.Builder(activity)
					    .setTitle(org.qaoverflow.q2android.R.string.new_question)
					    .setView(questionLayout)
					    .setPositiveButton(org.qaoverflow.q2android.R.string.submit, new DialogInterface.OnClickListener() {
			
							public void onClick(DialogInterface dialog, int whichButton) {
								HashMap<String,Object> data = new HashMap<String,Object>();
								HashMap<String,Object> info = new HashMap<String,Object>();

								info.put("type", "Q");
								info.put("title", title.getText().toString());
								//info.put("content", content.getText().toString());
                                info.put("content", e.getText().toString());

								info.put("tags", tags.getText().toString());
								info.put("format", (String) rawMap.get("format"));

								data.put("action_data", info);
								data.put("action","edit");
								data.put("postid", currentQuestionId);
								data.put("action_id", (String)rawMap.get("postid"));
								getQuestions(data,currentScope);

								hideKeyboard(title);
				        		isQuestion = false;
				        		adjustLayout();						

					        }
					    }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int whichButton) {
								hideKeyboard(questionLayout);
					        }
					    }).show();	
					}
					else {
	
						final EditText input = (EditText) layoutInflater.inflate(org.qaoverflow.q2android.R.layout.post_form, null);
						input.setText((String) rawMap.get("content"));
						new AlertDialog.Builder(activity)
					    .setTitle(org.qaoverflow.q2android.R.string.post_edit)
					    .setView(input)
					    .setPositiveButton(org.qaoverflow.q2android.R.string.submit, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int whichButton) {
								HashMap<String,Object> data = new HashMap<String,Object>();
								HashMap<String,Object> info = new HashMap<String,Object>();
								info.put("content", input.getText().toString());
								info.put("type", rawMap.get("basetype"));
								info.put("format", (String) rawMap.get("format"));
								
								data.put("action_data", info);
								data.put("postid", currentQuestionId);
								data.put("action","edit");
								data.put("action_id", (String)rawMap.get("postid"));
								getQuestion(data);
		
								hideKeyboard(input);
		
		
					        }
					    }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int whichButton) {
								hideKeyboard(input);
					        }
					    }).show();	
					}
				}
			});
			if(!first)
				buttons.addView(layoutInflater.inflate(org.qaoverflow.q2android.R.layout.button_separator, null));
			else
				first = false;
			buttons.addView(abutton);
		}
		if((Boolean)rawMap.get("flagbutton")) {
			Button abutton = (Button) layoutInflater.inflate(org.qaoverflow.q2android.R.layout.button, null);
			abutton.setText(getString(org.qaoverflow.q2android.R.string.flag));

			abutton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					HashMap<String,Object> data = new HashMap<String,Object>();
					HashMap<String,Object> info = new HashMap<String,Object>();
					info.put("question_id", currentQuestionId);
													
					data.put("action_data", info);						
					data.put("postid", currentQuestionId);
					data.put("action","flag");
					data.put("action_id", (String)rawMap.get("postid"));
					getQuestion(data);					
				}
			});
			if(!first)
				buttons.addView(layoutInflater.inflate(org.qaoverflow.q2android.R.layout.button_separator, null));
			else
				first = false;
			buttons.addView(abutton);
		}
		else if((Boolean)rawMap.get("unflaggable")) {
			Button abutton = (Button) layoutInflater.inflate(org.qaoverflow.q2android.R.layout.button, null);
			abutton.setText(getString(org.qaoverflow.q2android.R.string.unflag));

			abutton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					HashMap<String,Object> data = new HashMap<String,Object>();
					HashMap<String,Object> info = new HashMap<String,Object>();
					info.put("question_id", currentQuestionId);
													
					data.put("action_data", info);							
					data.put("postid", currentQuestionId);
					data.put("action","unflag");
					data.put("action_id", (String)rawMap.get("postid"));
					getQuestion(data);								
				}
			});
			if(!first)
				buttons.addView(layoutInflater.inflate(org.qaoverflow.q2android.R.layout.button_separator, null));
			else
				first = false;
			buttons.addView(abutton);
		}
		if((Boolean)rawMap.get("closeable")) {
		}
		if((Boolean)rawMap.get("hideable") || (Boolean)rawMap.get("reshowable")) {
			Button abutton = (Button) layoutInflater.inflate(org.qaoverflow.q2android.R.layout.button, null);
			abutton.setText(getString((Boolean)rawMap.get("hideable")? org.qaoverflow.q2android.R.string.hide: org.qaoverflow.q2android.R.string.reshow));

			abutton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View v) {
					HashMap<String,Object> data = new HashMap<String,Object>();
					HashMap<String,Object> info = new HashMap<String,Object>();
					if((Boolean)rawMap.get("hideable"))
						info.put("hide", "true");
													
					data.put("action_data", info);	
					data.put("postid", currentQuestionId);
					data.put("action","hide");
					data.put("action_id", (String)rawMap.get("postid"));
					getQuestion(data);	

				}
			});
			if(!first)
				buttons.addView(layoutInflater.inflate(org.qaoverflow.q2android.R.layout.button_separator, null));
			else
				first = false;
			buttons.addView(abutton);
		}
		if((Boolean)rawMap.get("deleteable")) {
			Button abutton = (Button) layoutInflater.inflate(org.qaoverflow.q2android.R.layout.button, null);
			abutton.setText(getString(org.qaoverflow.q2android.R.string.delete));

			abutton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View v) {
					
					HashMap<String,Object> data = new HashMap<String,Object>();
					data.put("postid", currentQuestionId);
					data.put("action","delete");
					data.put("action_id", (String)rawMap.get("postid"));
					getQuestion(data);	
				}
			});
			if(!first)
				buttons.addView(layoutInflater.inflate(org.qaoverflow.q2android.R.layout.button_separator, null));
			else
				first = false;
			buttons.addView(abutton);
		}

		
		if((Boolean)rawMap.get("answerbutton")) {
			Button abutton = (Button) layoutInflater.inflate(org.qaoverflow.q2android.R.layout.button, null);
			abutton.setText(getString(org.qaoverflow.q2android.R.string.answer));

			abutton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View v) {
					
					final EditText input = (EditText) layoutInflater.inflate(org.qaoverflow.q2android.R.layout.post_form, null);
					new AlertDialog.Builder(activity)
				    .setTitle(org.qaoverflow.q2android.R.string.post_answer)
				    .setView(input)
				    .setPositiveButton(org.qaoverflow.q2android.R.string.submit, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
							HashMap<String,Object> data = new HashMap<String,Object>();
							HashMap<String,Object> info = new HashMap<String,Object>();
							info.put("content", input.getText().toString());
							info.put("type", "A");
							info.put("question_id", currentQuestionId);
							info.put("parentid", currentQuestionId);
							info.put("format", prefs.getString("editor_type", ""));
							
							data.put("action_data", info);
							data.put("postid", currentQuestionId);
							data.put("action","post");
							data.put("action_id", (String)rawMap.get("postid"));
							getQuestion(data);
	
							hideKeyboard(input);
	
	
				        }
				    }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
							hideKeyboard(input);
				        }
				    }).show();	
				}
			});
			if(!first)
				buttons.addView(layoutInflater.inflate(org.qaoverflow.q2android.R.layout.button_separator, null));
			else
				first = false;
			buttons.addView(abutton);
		}
		
		if((Boolean)rawMap.get("commentbutton")) {
			Button cbutton = (Button) layoutInflater.inflate(org.qaoverflow.q2android.R.layout.button, null);
			cbutton.setText(getString(org.qaoverflow.q2android.R.string.comment));

			cbutton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
					final EditText input = (EditText) layoutInflater.inflate(org.qaoverflow.q2android.R.layout.post_form, null);
					new AlertDialog.Builder(activity)
				    .setTitle(org.qaoverflow.q2android.R.string.post_comment)
				    .setView(input)
				    .setPositiveButton(org.qaoverflow.q2android.R.string.submit, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
							HashMap<String,Object> data = new HashMap<String,Object>();
							HashMap<String,Object> info = new HashMap<String,Object>();
							info.put("content", input.getText().toString());
							info.put("type", "C");
							info.put("question_id", currentQuestionId);
							info.put("parentid", (String)rawMap.get("postid"));
							info.put("format", prefs.getString("editor_type", ""));

							data.put("action_data", info);
							data.put("postid", currentQuestionId);
							data.put("action","post");
							data.put("action_id", (String)rawMap.get("postid"));
							getQuestion(data);

							hideKeyboard(input);
				        }
				    }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
							hideKeyboard(input);
				        }
				    }).show();	
				}
			});
			if(!first)
				buttons.addView(layoutInflater.inflate(org.qaoverflow.q2android.R.layout.button_separator, null));
			else
				first = false;
			buttons.addView(cbutton);
		}

		return buttons;
		
	}
	
    /**
* Show all the accounts registered on the account manager. Request an auth token upon user select.
* @param authTokenType
*/
    private void showAccountPicker(final String authTokenType, final boolean invalidate) {

        final Account availableAccounts[] = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        
        final AccountManager acm = AccountManager.get(getApplicationContext());
        if (availableAccounts.length == 0) {

        	// create new account
            addNewAccount(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
            
        } else {
            final String name[] = new String[availableAccounts.length+2];
            for (int i = 0; i < availableAccounts.length; i++) {
                name[i] = availableAccounts[i].name;
            }
            name[name.length-2] = getString(org.qaoverflow.q2android.R.string.new_account);
            name[name.length-1] = getString(org.qaoverflow.q2android.R.string.manage_accounts);

            // Account picker
            new AlertDialog.Builder(this).setTitle("Pick Account").setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, name), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	if(which == name.length-2) {
                        addNewAccount(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
                	}
                	else if(which == name.length-1) {
                		Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
                		intent.putExtra(Settings.EXTRA_AUTHORITIES,new String[]{AccountGeneral.ACCOUNT_TYPE});
                		startActivity(intent);
                	}
                	else
                		getExistingAccountInfo(availableAccounts[which], authTokenType);
                }
            }).show();
        }
    }

    /**
	* Add new account to the account manager
	* @param accountType
	* @param authTokenType
	*/
    private void addNewAccount(String accountType, final String authTokenType) {
        mAccountManager.addAccount(accountType, authTokenType, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                	if(!future.isCancelled())
                		showAccountPicker(authTokenType, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, null);
    }

	private void getExistingAccountInfo(Account account, String authTokenType) {
    	final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, this, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();
                    String[] creds = bnd.getString(AccountManager.KEY_ACCOUNT_NAME).split("@");
                    
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username",creds[0]);
                    editor.putString("website",creds[1]);
                    editor.putString("password",bnd.getString(AccountManager.KEY_AUTHTOKEN));
                    editor.commit();
                    Message msg = new Message();
                    msg.what = MSG_SCOPE;
                    msg.arg1 = currentScope;
                    mHandler.sendMessage(msg);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String doubleEscapeTeX(String s) {
        String t="";
        for (int i=0; i < s.length(); i++) {
            if (s.charAt(i) == '\'') t += '\\';
            if (s.charAt(i) != '\n') t += s.charAt(i);
            if (s.charAt(i) == '\\') t += "\\";
        }
        return t;
    }

    private int exampleIndex = 0;
    private boolean mmltoggle = false;

    private String getExample(int index) {
        return getResources().getStringArray(org.qaoverflow.q2android.R.array.tex_examples)[index];
    }

    public void MathShow(View v){
        Log.d("onClick", "button2");

                EditText e = (EditText) findViewById(org.qaoverflow.q2android.R.id.edit);
                mmltoggle=false;
                w.loadUrl("javascript:document.getElementById('mmlout').innerHTML='';");
                w.loadUrl("javascript:document.getElementById('math').innerHTML='\\\\["
                        +doubleEscapeTeX(e.getText().toString())+"\\\\]';");
                w.loadUrl("javascript:MathJax.Hub.Queue(['Typeset',MathJax.Hub]);");

    }

    public void onClickBtn(int id) {
        Log.d("onClick", "onClick");
        if (id == 2) {
            Log.d("onClick", "button2");

            String content = edit.getText().toString();
            Log.d("button2", "content:"+content);

            String[] bloks = content.split("[$]");

            Log.d("button2", "bloks:"+bloks.length);
            mmltoggle=false;
            //initiateWebView(content);


            newText1.setText("");
            //w.evaluateJavascript("javascript:document.getElementById('math').innerHTML='\\\\[" +doubleEscapeTeX(content)+"\\\\]';",null);
            w.evaluateJavascript("javascript:document.getElementById('math').innerHTML='" +doubleEscapeTeX(content)+"';",null);
            //w.evaluateJavascript("javascript:document.getElementById('mmlout').innerHTML='';",null);
            newText2.setText("");

            w.loadUrl("javascript:MathJax.Hub.Queue(['Typeset',MathJax.Hub]);");
        }
        else if (id == 3) {
            //WebView w = (WebView) findViewById(R.id.webview);
            //EditText e = (EditText) findViewById(R.id.edit);
            mmltoggle=false;
            edit.setText("");
            //w.evaluateJavascript("javascript:document.getElementById('mmlout').innerHTML='';",null);
            w.evaluateJavascript("javascript:document.getElementById('math').innerHTML='';",null);
        }
        else if (id == 4) {
            //WebView w = (WebView) findViewById(R.id.webview);
            //EditText e = (EditText) findViewById(R.id.edit);
            Log.d("onClick", "button4");
            mmltoggle=false;
            edit.setText(getExample(exampleIndex++));
            if (exampleIndex > getResources().getStringArray(org.qaoverflow.q2android.R.array.tex_examples).length-1)
                exampleIndex=0;
            //w.evaluateJavascript("javascript:document.getElementById('mmlout').innerHTML='';",null);
            w.evaluateJavascript("javascript:document.getElementById('math').innerHTML='\\\\["
                    +doubleEscapeTeX(edit.getText().toString())
                    +"\\\\]';",null);
            w.loadUrl("javascript:MathJax.Hub.Queue(['Typeset',MathJax.Hub]);");
        }
    }




























    private void initiateWebView(String data){

        String content = edit.getText().toString();
        String[] bloks = content.split("$");

        String text1 = "";
        String text2 = "";
        String contents = "";

        switch (bloks.length){
            case 3:
                text1 = bloks[0];
                contents = bloks[1];
                text2 = bloks[2];
            break;

            case 2:
                text1 = bloks[0];
                contents = bloks[1];
            break;

            default:
                contents = data;
        }


        final String fText1 = text1;
        final String fText2 = text2;
        final String fData = contents;

        w.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                    loadUrlKitKat(fText1+fData+fText2);
                }
                else{
                    w.loadUrl("javascript:document.getElementById('math').innerHTML='"+fData+"';");
                }

                w.loadUrl("javascript:MathJax.Hub.Queue(['Typeset',MathJax.Hub]);");
            }
        });
        w.loadUrl("file:///android_asset/MathJax/index.html");

    }

    private void initiateAnswerWebView(){

        final String equationToBeDisplayedFinal = "";
        final String equationSymbolFinal = "";
        answerWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                    loadAnswerUrlKitKat(equationSymbolFinal+equationToBeDisplayedFinal);
                }
                else{
                    answerWebView.loadUrl("javascript:document.getElementById('math').innerHTML='<font color=\"yellow\">`"+equationToBeDisplayedFinal+"`</font>';");
                }

                answerWebView.loadUrl("javascript:MathJax.Hub.Queue(['Typeset',MathJax.Hub]);");
            }
        });



        answerWebView.loadDataWithBaseURL("http://bar/", "<script type='text/x-mathjax-config'>"
                +"MathJax.Hub.Config({ "
                +"showMathMenu: false, "
                +"jax: ['input/TeX','output/HTML-CSS'], " // output/SVG
                +"extensions: ['tex2jax.js','toMathML.js'], "
                +"TeX: { extensions: ['AMSmath.js','AMSsymbols.js',"
                +"'noErrors.js','noUndefined.js'] }, "
                //+"'SVG' : { blacker: 30, "
                // +"styles: { path: { 'shape-rendering': 'crispEdges' } } } "
                +"});</script>"
                +"<script type='text/javascript' "
                +"src='file:///android_asset/MathJax/MathJax.js'"
                +"></script>"
                +"<script type='text/javascript'>getLiteralMML = function() {"
                +"math=MathJax.Hub.getAllJax('math')[0];"
                // below, toMathML() rerurns literal MathML string
                +"mml=math.root.toMathML(''); return mml;"
                +"}; getEscapedMML = function() {"
                +"math=MathJax.Hub.getAllJax('math')[0];"
                // below, toMathMLquote() applies &-escaping to MathML string input
                +"mml=math.root.toMathMLquote(getLiteralMML()); return mml;}"
                +"</script>"
                +"<div id='text1'></div><span id='math'></span><pre><span id='mmlout'></span></pre><div id='text1'></div>","text/html","utf-8","");

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void loadAnswerUrlKitKat(String param){
        answerWebView.evaluateJavascript("javascript:document.getElementById('math').innerHTML='<font color=\"#97FD97\">`"+param+"`</font>';",null);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void loadUrlKitKat(String param){
        w.evaluateJavascript("javascript:document.getElementById('math').innerHTML='<font color=\"#97FD97\">`"+param+"`</font>';",null);
    }

    private String metaLinkToServer(String meta){
        String result = meta.replace("<a href=\"./user/", "<a href=\"http://gateoverflow.in/user/");
        //<a href="http://gateoverflow.in/user/Gate+Keeda
        //<a href="./user/
        result = Html.fromHtml(meta).toString();
        return result;
    }


}
/*
http://bankoverdraft.in
username: admin
password: admin123@

 */
