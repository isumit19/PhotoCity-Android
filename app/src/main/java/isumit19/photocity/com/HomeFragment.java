package isumit19.photocity.com;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private List<ImagePost> imagePostList;

    private FirebaseFirestore firebaseFirestore;

    private PostRecyclerAdapter postRecyclerAdapter;

    private FirebaseAuth firebaseAuth;

    private DocumentSnapshot lastVisible;

    private Boolean isFirstPageFirstLoad = true;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        imagePostList = new ArrayList<>();


        RecyclerView post_View = view.findViewById(R.id.postView);

        firebaseAuth = FirebaseAuth.getInstance();

        postRecyclerAdapter = new PostRecyclerAdapter(imagePostList);
        post_View.setLayoutManager(new LinearLayoutManager(container.getContext()));
        post_View.setAdapter(postRecyclerAdapter);

        if (firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            post_View.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if (reachedBottom) {
                        firequery();
                    }
                }
            });


            Query firstQ = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING);


            firstQ.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if (e == null && queryDocumentSnapshots!=null) {


                            if(isFirstPageFirstLoad && queryDocumentSnapshots.getDocuments().size()>0) {

                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                imagePostList.clear();
                            }

                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {


                                    String postId = doc.getDocument().getId();

                                    ImagePost imagePost = doc.getDocument().toObject(ImagePost.class).withId(postId);

                                    if(isFirstPageFirstLoad)
                                        imagePostList.add(imagePost);
                                    else
                                        imagePostList.add(0,imagePost);
                                    postRecyclerAdapter.notifyDataSetChanged();

                                }
                            }
                            isFirstPageFirstLoad = false;

                        }
                    }

            });
        }

        return view;
    }

    private void firequery() {

        if (lastVisible!=null&&firebaseAuth.getCurrentUser() != null) {

            Query nextQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible);

            nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (e == null) {

                        if (documentSnapshots!=null &&!documentSnapshots.isEmpty()) {

                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String PostId = doc.getDocument().getId();
                                    ImagePost blogPost = doc.getDocument().toObject(ImagePost.class).withId(PostId);
                                    imagePostList.add(blogPost);

                                    postRecyclerAdapter.notifyDataSetChanged();
                                }

                            }
                        }

                    }
                }
            });

        }

    }
    @Override
    public void onResume() {
        super.onResume();
        lastVisible = null;
        isFirstPageFirstLoad = true;
    }

}
