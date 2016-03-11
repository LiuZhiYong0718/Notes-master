package com.lguipeng.notes.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lguipeng.notes.App;
import com.lguipeng.notes.R;
import com.lguipeng.notes.injector.component.DaggerActivityComponent;
import com.lguipeng.notes.injector.module.ActivityModule;
import com.lguipeng.notes.model.SNote;
import com.lguipeng.notes.mvp.presenters.impl.NotePresenter;
import com.lguipeng.notes.mvp.views.impl.NoteView;
import com.lguipeng.notes.utils.DialogUtils;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import butterknife.Bind;

/**
 * Created by lgp on 2015/5/25.
 */
public class NoteActivity extends BaseActivity implements NoteView,View.OnClickListener{
    @Bind(R.id.btnAddPhoto)TextView mPhotoTextView;
    @Bind(R.id.btnAddVideo) TextView mVideoTextView;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.label_edit_text) MaterialEditText labelEditText;
    @Bind(R.id.content_edit_text) MaterialEditText contentEditText;
    @Bind(R.id.opr_time_line_text) TextView oprTimeLineTextView;

    @Inject NotePresenter notePresenter;
    private String currentPath = null;
    private MenuItem doneMenuItem;
    public static final int REQUEST_CODE_GET_PHOTO = 1;
    public static final int REQUEST_CODE_GET_VIDEO = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializePresenter();
        mPhotoTextView.setOnClickListener(this);
        mVideoTextView.setOnClickListener(this);
        notePresenter.onCreate(savedInstanceState);
    }

    private void initializePresenter() {
        notePresenter.attachView(this);
        notePresenter.attachIntent(getIntent());
    }

    @Override
    protected void initializeDependencyInjector() {
        App app = (App) getApplication();
        mActivityComponent = DaggerActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .appComponent(app.getAppComponent())
                .build();
        mActivityComponent.inject(this);
    }

    @Override
    protected void onStop() {
        notePresenter.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        notePresenter.onDestroy();
        super.onDestroy();
    }


    @Override
    protected int getLayoutView() {
        return R.layout.activity_note;
    }

    @Override
    protected void initToolbar(){
        super.initToolbar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        doneMenuItem = menu.getItem(0);
        notePresenter.onPrepareOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (notePresenter.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return notePresenter.onKeyDown(keyCode) || super.onKeyDown(keyCode, event);
    }

    @Override
    public void finishView() {
        finish();
    }

    @Override
    public void setToolbarTitle(String title) {
        if (toolbar != null){
            toolbar.setTitle(title);
        }
    }

    @Override
    public void setToolbarTitle(int title) {
        if (toolbar != null){
            toolbar.setTitle(title);
        }
    }

    /**
     * 初始化编辑模式(笔记的模式分为编辑模式)
     * @param note
     */
    @Override
    public void initViewOnEditMode(SNote note) {
        showKeyBoard();
        //获取焦点
        labelEditText.requestFocus();
        labelEditText.setText(note.getLabel());
        contentEditText.setText(note.getContent());
        labelEditText.setSelection(note.getLabel().length());
        contentEditText.setSelection(note.getContent().length());
        labelEditText.addTextChangedListener(notePresenter);
        contentEditText.addTextChangedListener(notePresenter);

    }

    /**
     * 初始化显示笔记模式
     * @param note
     */
    @Override
    public void initViewOnViewMode(SNote note) {
        //隐藏键盘
        hideKeyBoard();
        labelEditText.setText(note.getLabel());
        contentEditText.setText(note.getContent());
        labelEditText.setOnFocusChangeListener(notePresenter);
        contentEditText.setOnFocusChangeListener(notePresenter);
        labelEditText.addTextChangedListener(notePresenter);
        contentEditText.addTextChangedListener(notePresenter);
    }

    /**
     * 初始化创建模式
     * @param note
     */
    @Override
    public void initViewOnCreateMode(SNote note) {
        labelEditText.requestFocus();
        //labelEditText.addTextChangedListener(notePresenter);
        contentEditText.addTextChangedListener(notePresenter);
    }

    @Override
    public void setOperateTimeLineTextView(String text) {
        oprTimeLineTextView.setText(text);
    }

    @Override
    public void setDoneMenuItemVisible(boolean visible) {
        if (doneMenuItem != null){
            doneMenuItem.setVisible(visible);
        }
    }

    @Override
    public boolean isDoneMenuItemVisible() {
        return doneMenuItem != null && doneMenuItem.isVisible();
    }

    @Override
    public boolean isDoneMenuItemNull() {
        return doneMenuItem == null;
    }

    @Override
    public String getLabelText() {
        return labelEditText.getText().toString();
    }

    @Override
    public String getContentText() {
        return contentEditText.getText().toString();
    }

    /**
     *
     */
    @Override
    public void showNotSaveNoteDialog(){
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilder(this);
        builder.setTitle(R.string.not_save_note_leave_tip);
        builder.setPositiveButton(R.string.sure, notePresenter);
        builder.setNegativeButton(R.string.cancel, notePresenter);
        builder.show();
    }

    @Override
    public void hideKeyBoard(){
        hideKeyBoard(labelEditText);
    }

    @Override
    public void showKeyBoard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideKeyBoard(EditText editText){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
    /**
     * 获取存储Media的目录路径
     *
     * @return File类型的目录路径
     */
    public File getMediaDir() {
        File dir = new File(Environment.getExternalStorageDirectory(),
                "NotesMedia");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
    /**
     * 相应点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        Intent i;
        File f;
        switch (v.getId()){
            case R.id.btnAddPhoto :
                i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                f = new File(getMediaDir(), System.currentTimeMillis() + ".jpg");
                if (!f.exists()) {
                    try {
                        f.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                currentPath = f.getAbsolutePath();
                i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                startActivityForResult(i, REQUEST_CODE_GET_PHOTO);
                //Toast.makeText(getApplicationContext(),"bb",Toast.LENGTH_LONG).show();
                break;
            case R.id.btnAddVideo :
                // 使用Intent调用系统录像器，传入视频保存路径和名称
                i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                f = new File(getMediaDir(), System.currentTimeMillis() + ".mp4");
                if (!f.exists()) {
                    try {
                        f.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                currentPath = f.getAbsolutePath();
                i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

                startActivityForResult(i, REQUEST_CODE_GET_VIDEO);
//                Toast.makeText(getApplicationContext(),"dd",Toast.LENGTH_LONG).show();
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        System.out.println(data);

        switch (requestCode) {
            case REQUEST_CODE_GET_PHOTO:
            case REQUEST_CODE_GET_VIDEO:
                if (resultCode == RESULT_OK) {
//                    adapter.add(new MediaListCellData(currentPath));
//                    adapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
