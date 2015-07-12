package uit.linh.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import uit.linh.adapters.FontAdapter;
import uit.linh.providers.Fonts;

public class FontPicker extends AppCompatActivity implements AdapterView.OnItemClickListener {

    FontAdapter fontAdapter;
    ListView listViewFonts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font_picker);

        listViewFonts = (ListView) findViewById(R.id.listView_fonts);

        fontAdapter = new FontAdapter(this, R.layout.font_item, Fonts.fonts);
        listViewFonts.setAdapter(fontAdapter);


        listViewFonts.setOnItemClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_font_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = getIntent();
        intent.putExtra(MemeCreator.FLAG_FONT_CODE_RESULT, i);
        setResult(MemeCreator.RESULT_OK, intent);
        finish();
    }
}
