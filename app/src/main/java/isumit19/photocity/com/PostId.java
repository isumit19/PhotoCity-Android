package isumit19.photocity.com;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

class PostId {

    @Exclude
    String BlogPostId;

     <T extends PostId> T withId(@NonNull final String id) {
        this.BlogPostId = id;
        return (T) this;
    }

}