package com.example.ifind.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ifind.R;
import com.example.ifind.models.ModelComment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder>{

    Context context;
    List<ModelComment> commentList;
    String myUid, postId;

    public AdapterComments(Context context, List<ModelComment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind the row_comments.xml layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get the data
        String uid = commentList.get(position).getUid();
        String name = commentList.get(position).getuName();
        String email = commentList.get(position).getuEmail();
        String image = commentList.get(position).getuDp();
        String cid = commentList.get(position).getcId();
        String comment = commentList.get(position).getComment();
        String timestamp = commentList.get(position).getTimestamp();

        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //set the data
        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.timeTv.setText(pTime);
        //set user dp
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(holder.avatarIv);
        }
        catch (Exception e){
        }

        //comment click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if this comment is by currently signed in user or not
                if (myUid.equals(uid)){
                    //my comment
                    //show delete dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to delete this comment");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //delete comment
                            deleteComment(cid);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //dismiss dialog
                            dialogInterface.dismiss();
                        }
                    });
                    //show dialog
                    builder.create().show();
                }
                else {
                    //not my comment
                    Toast.makeText(context, "Can't delete other's comment...", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void deleteComment(String cid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue(); // it will delete the coment

        //now update the comments count
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ref.child("pComments");
                String comments = ""+snapshot.child("pComments").getValue();
                //int comments = 0;
                //int newCommentVal = comments - 1;
                int newCommentVal = Integer.parseInt(comments)-1;
                ref.child("pComments").setValue(""+newCommentVal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //
            }
        });

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        //declare views from row_comments.xml
        ImageView avatarIv;
        TextView nameTv, commentTv, timeTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            commentTv = itemView.findViewById(R.id.commentTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }

}
