package com.example.note;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView; //滑动控件
//    private FloatingActionButton mBtnAdd;
    private List<Note> notes;
    private RecyclerViewAdapter recyclerViewAdapter; //适配器
    private DatabaseHelper DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView(); //初始化滑动控件
        initData(); //查询数据
        initEvent(); //设置滑动控件
    }

    @Override
    protected void onResume() { //重新加载实例时调，用于添加完数据重新刷新数据库
        super.onResume();
        refreshDataFromDb();
    }

    private void refreshDataFromDb() { //刷新数据库
        notes = getDataFromDB();
        recyclerViewAdapter.refreshData(notes);
    }

    private void initView() { //获取列表控件
        recyclerView = findViewById(R.id.rectangles_1);
    }

    private void initData() { //查询数据
        notes = new ArrayList<>();
        DB = new DatabaseHelper(this);
        /*notes = getDataFromDB(); //查询数据 ，其实可以不用查询了，因为onResune还会在查一次*/
    }

    private void initEvent() { //完成recyclerView的配置
        recyclerViewAdapter = new RecyclerViewAdapter(this, notes); //初始化适配器
        recyclerView.setAdapter(recyclerViewAdapter); //完成设配器的设置
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this); //相当于设置线性布局
        recyclerView.setLayoutManager(linearLayoutManager); //放入滑动控件中
    }

    private List<Note> getDataFromDB() { //查询出数据库的全部数据
        return DB.queryAllFromDb();
    }

    public void add(View view) { //主界面的添加按钮的监听
        Intent intent = new Intent(MainActivity.this, AddActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //菜单项
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView(); //获取搜索框

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() { //搜索监听器
            @Override
            public boolean onQueryTextSubmit(String query) { //点击确定时
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { //输入框文字发生变化时
                notes = DB.queryFromDbByTitle(newText); //按标题进行查询
                recyclerViewAdapter.refreshData(notes); //查完刷新
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}