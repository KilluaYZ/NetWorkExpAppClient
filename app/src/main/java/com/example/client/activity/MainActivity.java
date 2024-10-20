package com.example.client.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.client.R;
import com.example.client.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

	private MainViewModel mainViewModel;
	private Button btnSend;
	private Button btnReceive;
	private Button btnConnect;
	private EditText etIP;
	private EditText etPort;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		mainViewModel = new MainViewModel(this.getApplication());
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

		btnConnect.setOnClickListener(v -> {
			mainViewModel.isConnected = !mainViewModel.isConnected;
			// TODO:连接/断开连接，更新UI
		});

		btnSend.setOnClickListener(v -> {
			// TODO:发送数据
		});

		btnReceive.setOnClickListener(v -> {
			// TODO:接收数据
		});
	}
}