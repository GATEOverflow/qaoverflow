
/*
import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.graphics.*;
import android.content.res.*;
import android.webkit.*;
import android.text.method.*;
import android.text.*;
import android.content.*;

public class MainActivity extends Activity
implements View.OnClickListener
{
	
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
		return getResources().getStringArray(R.array.tex_examples)[index];
	}

	public void onClick(View v) {
		if (v == findViewById(R.id.button2)) {
			WebView w = (WebView) findViewById(R.id.webview);
			EditText e = (EditText) findViewById(R.id.edit);
			mmltoggle=false;
			w.loadUrl("javascript:document.getElementById('mmlout').innerHTML='';");
			w.loadUrl("javascript:document.getElementById('math').innerHTML='\\\\["
			           +doubleEscapeTeX(e.getText().toString())+"\\\\]';");
			w.loadUrl("javascript:MathJax.Hub.Queue(['Typeset',MathJax.Hub]);");
		}
		else if (v == findViewById(R.id.button3)) {
			WebView w = (WebView) findViewById(R.id.webview);
			EditText e = (EditText) findViewById(R.id.edit);
			mmltoggle=false;
			e.setText("");
			w.loadUrl("javascript:document.getElementById('mmlout').innerHTML='';");
			w.loadUrl("javascript:document.getElementById('math').innerHTML='';");
		}
		else if (v == findViewById(R.id.button4)) {
			WebView w = (WebView) findViewById(R.id.webview);
			EditText e = (EditText) findViewById(R.id.edit);
			mmltoggle=false;
			e.setText(getExample(exampleIndex++));
			if (exampleIndex > getResources().getStringArray(R.array.tex_examples).length-1) 
				exampleIndex=0;
			w.loadUrl("javascript:document.getElementById('mmlout').innerHTML='';");
			w.loadUrl("javascript:document.getElementById('math').innerHTML='\\\\["
			          +doubleEscapeTeX(e.getText().toString())
					  +"\\\\]';");
			w.loadUrl("javascript:MathJax.Hub.Queue(['Typeset',MathJax.Hub]);");
		}
		else if (v == findViewById(R.id.button5)) {
			WebView w = (WebView) findViewById(R.id.webview);
			EditText e = (EditText) findViewById(R.id.edit);
			mmltoggle=!mmltoggle;
			w.loadUrl("javascript:document.getElementById('mmlout').innerHTML='';");
			// need 2 versions of the MathML
			// showMML() returns literal MathML, getMML() returns &-escaped for HTML display
			// put getMML() into innerHTML of mmlout span
			// use JS call to clipMML() method in injected Java object 
			// to put showMML() into system clipboard
			if (mmltoggle) {
				// &-escaped MathML enclosed in <pre> tags in "mmlout" span for HTML display
				w.loadUrl("javascript:document.getElementById('mmlout').innerHTML = window.getEscapedMML();");
				w.loadUrl("javascript:injectedObject.clipMML(window.getLiteralMML());");
			}
		}	
	}

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		WebView w = (WebView) findViewById(R.id.webview);
		w.getSettings().setJavaScriptEnabled(true);
		w.getSettings().setBuiltInZoomControls(true);
		w.loadDataWithBaseURL("http://bar/", "<script type='text/x-mathjax-config'>"
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
							  +"<span id='math'></span><pre><span id='mmlout'></span></pre>","text/html","utf-8","");
		w.addJavascriptInterface(new Object() { public void clipMML(String s) {
					WebView ww = (WebView) findViewById(R.id.webview);	
					//uses android.text.ClipboardManager for compatibility with pre-Honeycomb
					//for HC or later, use android.content.ClipboardManager
					android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					//next 2 comment lines have HC or later code, can also try newHtmlText()
					//ClipData clip = ClipData.newPlainText("MJ MathML text",s);//,s);
					//clipboard.setPrimaryClip(clip);
					// literal MathML (in parameter s) placed on system clipboard
					clipboard.setText(s);
					Toast.makeText(getApplicationContext(),"MathML copied to clipboard",Toast.LENGTH_SHORT).show();
		    }}, "injectedObject");
		EditText e = (EditText) findViewById(R.id.edit);
		e.setBackgroundColor(Color.LTGRAY);
		e.setTextColor(Color.BLACK);
		e.setText("");
		Button b = (Button) findViewById(R.id.button2);
		b.setOnClickListener(this);
		b = (Button) findViewById(R.id.button3);
		b.setOnClickListener(this);
		b = (Button) findViewById(R.id.button4);
		b.setOnClickListener(this);
		b = (Button) findViewById(R.id.button5);
		b.setOnClickListener(this);
		TextView t = (TextView) findViewById(R.id.textview3);
		t.setMovementMethod(LinkMovementMethod.getInstance());
		t.setText(Html.fromHtml(t.getText().toString()));	
		
	}
}

*/