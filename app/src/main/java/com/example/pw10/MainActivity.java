package com.example.pw10;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText editTextName, editTextEmail;
    TextView textViewUserName;

    DBHelper dbHelper;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        textViewUserName = findViewById(R.id.textViewUserName);

        dbHelper = new DBHelper(this);
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // Загрузка сохраненных контактов
        loadContacts();

        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonSearch = findViewById(R.id.buttonSearch);
        Button buttonUpdate = findViewById(R.id.buttonUpdate);
        Button buttonDelete = findViewById(R.id.buttonDelete);

        // Сохранение контакта
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                addContact(name, email);
            }
        });

        // Поиск контакта
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                searchContact(name, email); // Вызываем метод поиска контакта по имени или почте
            }
        });

        // Обновление контакта
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                updateContact(name, email);
            }
        });

        // Удаление контакта
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();
                deleteContact(name);
            }
        });
    }

    public void addContact(String name, String email) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_NAME, name);
        values.put(DBHelper.COLUMN_EMAIL, email);
        long newRowId = db.insert(DBHelper.TABLE_CONTACTS, null, values);
        db.close();
        if (newRowId != -1) {
            Toast.makeText(this, "Контакт добавлен", Toast.LENGTH_SHORT).show();
            // После добавления контакта перезагрузим список контактов
            loadContacts();
            // Сохранение введенных данных в SharedPreferences
            savePreferences(name, email);
        } else {
            Toast.makeText(this, "Ошибка добавления контакта", Toast.LENGTH_SHORT).show();
        }
    }

    // Метод для поиска контакта по имени
    // Метод для поиска контакта по имени или почте
    public void searchContact(String name, String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                DBHelper.COLUMN_NAME,
                DBHelper.COLUMN_EMAIL
        };

        // Проверяем, какое поле для поиска было заполнено
        String selection;
        String[] selectionArgs;
        if (!name.isEmpty()) {
            selection = DBHelper.COLUMN_NAME + " LIKE ?";
            selectionArgs = new String[]{"%" + name + "%"};
        } else if (!email.isEmpty()) {
            selection = DBHelper.COLUMN_EMAIL + " LIKE ?";
            selectionArgs = new String[]{"%" + email + "%"};
        } else {
            // Если оба поля пусты, просто выходим из метода
            Toast.makeText(this, "Введите имя или почту для поиска", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = db.query(
                DBHelper.TABLE_CONTACTS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        StringBuilder contacts = new StringBuilder();
        while (cursor.moveToNext()) {
            String contactName = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NAME));
            String contactEmail = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_EMAIL));
            contacts.append(contactName).append(", ").append(contactEmail).append("\n");
        }
        cursor.close();

        if (contacts.length() > 0) {
            Toast.makeText(this, "Результат поиска:\n" + contacts.toString(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Контакт не найден", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateContact(String name, String newEmail) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_EMAIL, newEmail);

        String selection = DBHelper.COLUMN_NAME + " LIKE ?";
        String[] selectionArgs = { name };

        int count = db.update(
                DBHelper.TABLE_CONTACTS,
                values,
                selection,
                selectionArgs);

        db.close();

        if (count > 0) {
            Toast.makeText(this, "Контакт успешно обновлен", Toast.LENGTH_SHORT).show();
            // После обновления контакта перезагрузим список контактов
            loadContacts();
        } else {
            Toast.makeText(this, "Не удалось обновить контакт", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteContact(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = DBHelper.COLUMN_NAME + " LIKE ?";
        String[] selectionArgs = { name };

        int deletedRows = db.delete(DBHelper.TABLE_CONTACTS, selection, selectionArgs);
        db.close();

        if (deletedRows > 0) {
            Toast.makeText(this, "Контакт успешно удален", Toast.LENGTH_SHORT).show();
            // После удаления контакта перезагрузим список контактов
            loadContacts();
        } else {
            Toast.makeText(this, "Не удалось удалить контакт", Toast.LENGTH_SHORT).show();
        }
    }

    // Метод для загрузки списка контактов
    public void loadContacts() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                DBHelper.COLUMN_NAME,
                DBHelper.COLUMN_EMAIL
        };

        Cursor cursor = db.query(
                DBHelper.TABLE_CONTACTS,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        StringBuilder contacts = new StringBuilder();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NAME));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_EMAIL));
            contacts.append("Имя: ").append(name).append(", Email: ").append(email).append("\n");
        }
        cursor.close();

        textViewUserName.setText(contacts.toString());
    }

    // Метод для сохранения введенных данных в SharedPreferences
    private void savePreferences(String name, String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.apply();
    }
}
