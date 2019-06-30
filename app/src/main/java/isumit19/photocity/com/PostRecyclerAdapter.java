package isumit19.photocity.com;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder> {

    private List<ImagePost> imagePostList;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;


    PostRecyclerAdapter(List<ImagePost> imagePostsList){
        imagePostList = imagePostsList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.postlist_item,parent,false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        //holder.setIsRecyclable(false);

        if(firebaseAuth.getCurrentUser()!=null) {

            final String postId = imagePostList.get(position).BlogPostId;
            final String currentUid = firebaseAuth.getCurrentUser().getUid();
            String desc_data = imagePostList.get(position).getDesc();
            holder.setDescText(desc_data);

            String image_uri = imagePostList.get(position).getImage_url();
            String thumb_url = imagePostList.get(position).getThumb_url();
            holder.setPostImage(image_uri, thumb_url);

            final String user_id = imagePostList.get(position).getUser_id();

            firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {

                        String userName = task.getResult().getString("name");
                        String userImage = task.getResult().getString("image");
                        holder.setUserData(userName, userImage);
                    } else {
                        //Error
                    }
                }
            });

            long millisec = imagePostList.get(position).getTimestamp().getTime();
            String dateString = new SimpleDateFormat("dd.MM.yyyy").format(new Date(millisec));
            holder.setTimeStamp(dateString);

            //count
            firebaseFirestore.collection("Posts/" + postId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                        int count = queryDocumentSnapshots.size();
                        holder.setLike_count(count);

                    } else {
                        holder.setLike_count(0);
                    }
                }
            });


            //getLikes
            firebaseFirestore.collection("Posts/" + postId + "/Likes").document(currentUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                    if (documentSnapshot!=null&&documentSnapshot.exists()) {
                        holder.like_btn.setImageDrawable(context.getDrawable(R.mipmap.action_like_accent));
                    } else {
                        holder.like_btn.setImageDrawable(context.getDrawable(R.mipmap.action_like_gray));

                    }
                }
            });


            //likes

            holder.like_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    firebaseFirestore.collection("Posts/" + postId + "/Likes").document(currentUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.getResult()!=null && !task.getResult().exists()) {
                                Map<String, Object> likesMap = new HashMap<>();

                                likesMap.put("timestamp", FieldValue.serverTimestamp());
                                firebaseFirestore.collection("Posts/" + postId + "/Likes").document(currentUid).set(likesMap);
                                //      holder.like_btn.setImageDrawable(context.getDrawable(R.drawable.ic_favorite_accent_24dp));


                            } else {
                                firebaseFirestore.collection("Posts/" + postId + "/Likes").document(currentUid).delete();
                                //     holder.like_btn.setImageDrawable(context.getDrawable(R.drawable.ic_favorite_grey_24dp));

                            }
                        }
                    });


                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return imagePostList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {


        View mView;

        TextView captionView, UserName, TimeStamp;
        ImageView postImage;
        ImageView like_btn;
        TextView like_count;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            like_btn = mView.findViewById(R.id.like_btn);
        }

        void setDescText(String text){
            captionView = mView.findViewById(R.id.postdesc);
            captionView.setText(text);
        }

        void setPostImage(String Image_uri, String thum_url){

            postImage = mView.findViewById(R.id.postimage);
            RequestOptions placeholder = new RequestOptions();
            placeholder.placeholder(R.drawable.image_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholder).load(Image_uri).thumbnail(Glide.with(context).load(thum_url)).into(postImage);


        }
        void setTimeStamp(String timeStamp){
            TimeStamp = mView.findViewById(R.id.postdate);
            TimeStamp.setText(timeStamp);
        }
        void setUserData(String name, String image){
            UserName = mView.findViewById(R.id.user_name);
            CircleImageView userimage = mView.findViewById(R.id.user_image);

            UserName.setText(name);

            RequestOptions placeholder = new RequestOptions();
            placeholder.placeholder(R.drawable.profile_placeholder);


            Glide.with(context).applyDefaultRequestOptions(placeholder).load(image).into(userimage);

        }

        void setLike_count(int count){
            like_count = mView.findViewById(R.id.like_count);
            like_count.setText(count + " Likes");
        }

    }
}
