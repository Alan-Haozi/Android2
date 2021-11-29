package com.example.note;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<Note> List;
    private LayoutInflater layoutinflater; //负责加载布局，setContentView方法的底层
    private Context Context; //一个抽象类，上下文
    private DatabaseHelper noteDB;

    public RecyclerViewAdapter(Context context, List<Note> mlist) { //构造函数，传入要展示的数据源list
        this.List = mlist;
        this.Context = context;
        layoutinflater = LayoutInflater.from(context); //实例化
        noteDB = new DatabaseHelper(context);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView mTvTitle; //定义每个控件
        TextView mTvAuthor;
        TextView mTvContent;
        TextView mTvTime;
        ViewGroup item; //定义每个item的容器

        public ViewHolder(@NonNull View itemView) { //view是RecyclerView子项的最外层布局
            super(itemView);
            this.mTvTitle = itemView.findViewById(R.id.RtextView_title); //分别找到布局中的实例
            this.mTvAuthor = itemView.findViewById(R.id.RtextView_author);
            this.mTvContent = itemView.findViewById(R.id.RtextView_content);
            this.mTvTime = itemView.findViewById(R.id.RtextView_time);
            this.item = itemView.findViewById(R.id.relativelayout);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //用于创建ViewHolder实例
        View view = layoutinflater.inflate(R.layout.item, parent, false); //加载布局
        ViewHolder recyclerMyViewHolder = new ViewHolder(view); //传入布局
        return recyclerMyViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) { //点击列表项进行编辑，也就是对RecyclerView的子项数据进项赋值

        /*ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT; //使每个item不占满屏幕*/

        Note note = List.get(position);

        holder.mTvTitle.setText(note.getTitle()); //依次给每个数据项
        holder.mTvAuthor.setText(note.getAuthor());
        holder.mTvContent.setText(note.getContent());
        holder.mTvTime.setText(note.getTime());

        holder.item.setOnClickListener(new View.OnClickListener() { //轻点功能，给列表项设置监听，跳转到编辑界面
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Context, EditActivity.class);
                intent.putExtra("id", note.getId()); //传入note数据
                Context.startActivity(intent);
            }
        });

        holder.item.setOnLongClickListener(new View.OnLongClickListener() { //长按功能，长按弹窗功能
            @Override
            public boolean onLongClick(View v) {
                Dialog dialog = new Dialog(Context);
                View view = layoutinflater.inflate(R.layout.item_dialog, null);

                TextView delect = view.findViewById(R.id.dia_textView_delete); //获取孔健
                TextView edit = view.findViewById(R.id.dia_textView_edit);

                delect.setOnClickListener(new View.OnClickListener() { //删除按钮的操作
                    @Override
                    public void onClick(View v) {
                        int row = noteDB.deleteData(note.getId());
                        if (row > 0) {
                            removeData(position);
                            Toast.makeText(Context, "删除成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Context, "删除失败", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss(); //关闭长按弹窗
                    }
                });

                edit.setOnClickListener(new View.OnClickListener() { //编辑按钮的操作
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Context, EditActivity.class);
                        intent.putExtra("id", note.getId());
                        Context.startActivities(new Intent[]{intent});
                        dialog.dismiss(); //关闭长按弹窗
                    }
                });

                dialog.setContentView(view);
                dialog.show();

                return false;
            }
        });
    }

    @Override
    public int getItemCount() { //返回RecyclerView一共有多少子项
        return List.size();
    }

    public void refreshData(List<Note> notes) { //更新数据
        this.List = notes;
        notifyDataSetChanged(); //刷新列表
    }

    public void removeData(int pos) { //删除数据
        List.remove(pos); //删除链表里面的数据
        notifyItemRemoved(pos); //删除这个列表
    }
}
