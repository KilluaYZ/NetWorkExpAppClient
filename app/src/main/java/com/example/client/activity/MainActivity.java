package com.example.client.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.client.R;
import com.example.client.fragment.FileDialog;
import com.example.client.utils.MSocketClient;
import com.example.client.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

	private static final int REQUEST_CODE_PICK_FILE = 11;

	private MainViewModel mainViewModel;
	private Button btnSend;
	private Button btnReceive;
	private Button btnConnect;
	private EditText etIP;
	private EditText etPort;
	private TextView tvSignal;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
		initObserver();
	}


	private void initView() {
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_main);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		btnSend = findViewById(R.id.btnSend);
		btnReceive = findViewById(R.id.btnReceive);
		btnConnect = findViewById(R.id.btnConnect);
		etIP = findViewById(R.id.etIP);
		etPort = findViewById(R.id.etPort);
		tvSignal = findViewById(R.id.tvSignal);

		btnConnect.setOnClickListener(v -> {
				mainViewModel.mSocketClient = new MSocketClient();
				mainViewModel.mSocketClient.connect(etIP.getText().toString(), Integer.parseInt(etPort.getText().toString()), this, mainViewModel);

			// TODO:连接/断开连接，更新UI
		});

		btnSend.setOnClickListener(v -> {
			mainViewModel.action = MainViewModel.SEND;
			// TODO:发送数据
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			// 设置文件类型，例如选择所有类型的文件
			intent.setType("*/*");
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			// 启动文件选择器
			startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
		});

		btnReceive.setOnClickListener(v -> {
			// TODO:接收数据
			Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
			// 设置文件类型，例如选择所有类型的文件
			intent.setType("*/*");
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.putExtra(Intent.EXTRA_TITLE, "NetWorkExp.txt");
			// 启动文件选择器
			startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
			mainViewModel.action = MainViewModel.RECEIVE;
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK && data != null) {
			// 获取选中的文件Uri
			Uri selectedFileUri = data.getData();
			mainViewModel.outFileUriPath = selectedFileUri;
			showCustomDialog();
		}
	}

	// 在Activity或Fragment中
	public void showCustomDialog() {
		FileDialog fileDialog = new FileDialog(this);
 		fileDialog.show(getSupportFragmentManager(), "fileDialog");
	}

	private void initObserver() {
		mainViewModel.isConnected.observe(this, aBoolean -> {
			// TODO:连接状态改变，更新UI
			if (aBoolean) {
				Toast.makeText(this, "连接成功", Toast.LENGTH_SHORT).show();
				tvSignal.setText(getString(R.string.connected));

			}
		});
	}
}