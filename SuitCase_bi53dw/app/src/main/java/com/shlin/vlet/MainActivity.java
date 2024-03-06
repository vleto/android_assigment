package com.shlin.vlet;

import androidx.appcompat.app.AppCompatActivity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yanzhenjie.album.Action;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    FirebaseStorage firebaseStorage;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    CollectionReference productRef = firebaseFirestore.collection("PRODUCT");

    ArrayList<ProductModel> productModels = new ArrayList<>();
    CollectionDataFromStorage collectionDataFromStorage;
    private String selectedId = "";
    EditText etName, etPrice, etDescription;
    Button btnImageUpload, btnAdd, btnEdit, btnDelete;
    TextView tvImagePath;
    ListView lvProducts;
    CheckBox cbPurchased;
    URL imageUrl = null;
    String  productImageUrl = "";
    String filePath = "";
    StorageReference storageRef ;
    private Intent loginActivity;
    private SensorManager sensorManager;
    private long lastUpdated;
    GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lastUpdated = System.currentTimeMillis();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        loginActivity = new Intent(this, LoginActivity.class);

        firebaseStorage = FirebaseStorage.getInstance();
        storageRef= firebaseStorage.getReferenceFromUrl("gs://shlin-suitcase.appspot.com");

        etName = findViewById(R.id.etProductName);
        etPrice = findViewById(R.id.etProductPrice);
        etDescription = findViewById(R.id.etProductDescription);
        cbPurchased = findViewById(R.id.cbPurchased);

        btnAdd = findViewById(R.id.btnAdd);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);


        btnImageUpload = findViewById(R.id.btnImageUpload);
        btnImageUpload.setOnClickListener(v -> chooseImage());

        tvImagePath = findViewById(R.id.tvImagePath);

        lvProducts = findViewById(R.id.lvProducts);
        lvProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProductModel productModel = collectionDataFromStorage.getItem(position);
                etName.setText(productModel.getName());
                etDescription.setText(productModel.getDescription());
                etPrice.setText(productModel.getPrice());
                cbPurchased.setChecked(productModel.getPurchased().equals("Purchased") ? true : false);
                tvImagePath.setText(productModel.getFilePath());
                productImageUrl = productModel.getFilePath();
                selectedId = productModel.getDocId();
            }
        });
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                handleDoubleTap();
                return true;
            }
        });



    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private void handleDoubleTap() {
        clearTextFields();
        Toast.makeText(this, "Double Tap Detected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }

        // Set a listener for the SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle search query submission (if needed)
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle search query text changes (perform filtering)
                filterProducts(newText);
                return true;
            }
        });

        return true;
    }

    private void filterProducts(String query) {
        ArrayList<ProductModel> filteredProducts = new ArrayList<>();

        for (ProductModel product : productModels) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredProducts.add(product);
            }
        }

        // Update the ListView with filtered products
        collectionDataFromStorage = new CollectionDataFromStorage(MainActivity.this, filteredProducts);
        collectionDataFromStorage.notifyDataSetChanged();
        lvProducts.setAdapter(collectionDataFromStorage);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            logOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logOut() {
        firebaseAuth.signOut();
        try {
            Authentication authentication = new Authentication(MainActivity.this);
            authentication.logoutSession();
            refreshUI();
        } catch (Exception e) {
            showMessage(e.getMessage());
        }
    }

    private void refreshUI() {
        startActivity(loginActivity);
        finish();
    }

    private void showMessage(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    private void chooseImage() {
        Album.image(this) // Image selection.
                .multipleChoice()
                .camera(true)
                .columnCount(3)
                .selectCount(1)
                .onResult(new Action<ArrayList<AlbumFile>>() {
                    @Override
                    public void onAction(@NonNull ArrayList<AlbumFile> result) {
                        filePath = result.get(0).getPath();
                        tvImagePath.setText(filePath);
                    }
                })
                .onCancel(new Action<String>() {
                    @Override
                    public void onAction(@NonNull String result) {
                        // Operation canceled
                    }
                })
                .start();
    }

    @Override
    protected void onStart() {
        super.onStart();

        productRef.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) { return; }

                productModels.clear();

                assert value != null;
                for (QueryDocumentSnapshot documentSnapshot : value) {
                    ProductModel productModel = documentSnapshot.toObject(ProductModel.class);
                    productModel.setDocId(documentSnapshot.getId());

                    if (productModel.getUserId().equals(currentUser.getUid())) {
                        productModels.add(productModel);
                    }
                }

                collectionDataFromStorage = new CollectionDataFromStorage(MainActivity.this, productModels);
                collectionDataFromStorage.notifyDataSetChanged();
                lvProducts.setAdapter(collectionDataFromStorage);
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tvImagePath.getText().toString().isEmpty()) {
                    Uri uri = Uri.fromFile(new File(filePath));
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                    String dateTime = sdf.format(new Date());

                    final StorageReference childRef = storageRef.child(dateTime);
                    UploadTask uploadTask = childRef.putFile(uri);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            childRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    productImageUrl = task.getResult().toString();
                                    Log.v("URL", productImageUrl);
                                    addProduct();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Error", e.getLocalizedMessage());
                            Toast.makeText(MainActivity.this, "Upload Failed -> " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    addProduct();
                }
            }
        });

        btnEdit.setOnClickListener(this::updateProduct);

        btnDelete.setOnClickListener(this::deleteProduct);

    }

    public void addProduct() {
        String name = etName.getText().toString();
        String description = etDescription.getText().toString();
        String price = etPrice.getText().toString();
        String purchased = cbPurchased.isChecked() ? "Purchased" : "Not purchased";

        Map<String, Object> product = new HashMap<>();
        product.put("name", name);
        product.put("description", description);
        product.put("price", price);
        product.put("purchased", purchased);
        product.put("filePath", productImageUrl);
        product.put("userId", currentUser.getUid());
        if (!name.isEmpty()) {
            try {
                productRef.add(product);

                etName.setText("");
                etDescription.setText("");
                etPrice.setText("");
                tvImagePath.setText("");
                productImageUrl = "";

                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Error" + e, Toast.LENGTH_LONG).show();
                Log.e("Error", e.getLocalizedMessage());
            }
        } else {
            Toast.makeText(this, "Please select a product", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateProduct(View view) {
        String name = etName.getText().toString();
        String description = etDescription.getText().toString();
        String price = etPrice.getText().toString();
        String purchased = cbPurchased.isChecked() ? "Purchased" : "Not purchased";

        String filePath = productImageUrl;
        ProductModel productModel = new ProductModel(name, description, price, purchased, filePath, currentUser.getUid());

        if (!name.isEmpty()) {
            try {
                productRef.document(selectedId).set(productModel);

                etName.setText("");
                etDescription.setText("");
                etPrice.setText("");
                tvImagePath.setText("");
                productImageUrl = "";

                Toast.makeText(MainActivity.this, "Updated ", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Error " + e, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Please select a productModel", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteProduct(View view) {
        if (!selectedId.equals("")) {
            try {
                productRef.document(selectedId).delete();

                etName.setText("");
                etDescription.setText("");
                etPrice.setText("");
                tvImagePath.setText("");
                productImageUrl = "";

                Toast.makeText(MainActivity.this, "Deleted ", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Error " + e, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Please select a product", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 71 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            File myFile = new File(data.getData().getPath());

            try {
                imageUrl = myFile.toURI().toURL();
                tvImagePath.setText(imageUrl.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] values = sensorEvent.values;
            float x = values[0];
            float y = values[1];
            float z = values[2];

            float EG = SensorManager.GRAVITY_EARTH;
            float deviceAccel = (x*x+y*y+z*z)/(EG*EG);

            if (deviceAccel >= 1.7) {
                long actualTime = System.currentTimeMillis();
                if ((actualTime - lastUpdated) > 1000) {
                    clearTextFields();
                }
            }
        }
    }

    private void clearTextFields() {
        etName.setText("");
        etDescription.setText("");
        etPrice.setText("");
        tvImagePath.setText("");
        productImageUrl = "";
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not applicable.
    }

    @Override
    protected void onStop() {
        super.onStop();

        sensorManager.unregisterListener(this);
    }

}