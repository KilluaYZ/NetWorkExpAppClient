package com.example.client.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.example.client.R;
import com.example.client.activity.MainActivity;
import com.example.client.utils.MBinaryFileManager;
import com.example.client.utils.MFrame;
import com.example.client.viewmodel.MainViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Objects;

public class FileDialog extends DialogFragment {
	private MaterialAlertDialogBuilder builder;
	private MainViewModel mViewModel;
	private ProgressBar progressBar;
	private TextView tvSum;
	private TextView tvCount;
	private TextView tvTitle;
	private Handler handler;
	private MainActivity activity;
	private View dialogView;
	private LottieAnimationView lottieAnimProgress;
	private TextView tvSpeed;
	private long startTime;
	public FileDialog(MainActivity activity) {
		this.activity = activity;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		handler = new Handler(Looper.getMainLooper());
		initViews();
		if (activity != null) {
			mViewModel = new ViewModelProvider(activity).get(MainViewModel.class);
		}
		return dialogView;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		LayoutInflater inflater = requireActivity().getLayoutInflater();
		// 加载自定义布局
		dialogView = inflater.inflate(R.layout.dialog, null);
		builder = new MaterialAlertDialogBuilder(requireContext());
		builder.setView(dialogView);
		startTime = System.currentTimeMillis();
		return builder.create();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// 在视图创建后初始化观察者
		initObserver();
		switch (mViewModel.action) {
			case MainViewModel.SEND:
				tvTitle.setText(R.string.sending);
				sendFile();
				break;
			case MainViewModel.RECEIVE:
				tvTitle.setText(R.string.receiving);
				receiveFile();
				break;
			default:
				break;
		}
	}

	private void initViews() {
		// 设置自定义布局
		progressBar = dialogView.findViewById(R.id.progressBar);
		tvSum = dialogView.findViewById(R.id.tvSum);
		tvCount = dialogView.findViewById(R.id.tvCount);
		tvTitle = dialogView.findViewById(R.id.tvTitle);
		lottieAnimProgress = dialogView.findViewById(R.id.lottieAnimProgress);
		tvSpeed = dialogView.findViewById(R.id.tvSpeed);
	}

	private void sendFile() {
		new Thread(() -> {
			// 大文件传输包拆分和拼接的Manager
			if (getActivity() == null) {
				return;
			}
			MBinaryFileManager mBinaryFileManager = new MBinaryFileManager(getActivity(), mViewModel.outFileUriPath);
			mViewModel.mSocketClient.initSend();
			while (mBinaryFileManager.has_next()) {
				mViewModel.mSocketClient.sendSyn(mBinaryFileManager, () -> {
					int totalPackageNum = mBinaryFileManager.getTotalNum();
					int curPackageNum = mBinaryFileManager.getCurNum();
					activity.runOnUiThread(() -> {
						mViewModel.packageSum.setValue(totalPackageNum);
						mViewModel.packageCount.setValue(curPackageNum);
					});
					return null;
				});
			}
		}).start();
	}

	private void receiveFile() {
		new Thread(() -> {
			// 大文件传输包拆分和拼接的Manager
			if (getActivity() == null) {
				return;
			}
			MBinaryFileManager mBinaryFileManager = new MBinaryFileManager(getActivity(), mViewModel.outFileUriPath);
			mViewModel.mSocketClient.initReceive();
			while (mBinaryFileManager.has_next()) {
				mViewModel.mSocketClient.receiveSyn(mBinaryFileManager, () -> {
					int totalPackageNum = mBinaryFileManager.getTotalNum();
					int curPackageNum = mBinaryFileManager.getCurNum();
					activity.runOnUiThread(() -> {
						mViewModel.packageSum.setValue(totalPackageNum);
						mViewModel.packageCount.setValue(curPackageNum);
					});
					return null;
				});
			}
		}).start();
	}

	/**
	 * 监听ViewModel中的数据，当packageCount发生变化，就更新进度条
	 */
	private void initObserver() {
		mViewModel.packageCount.observe(getViewLifecycleOwner(), integer -> {
			if (mViewModel.packageSum.getValue() == null || mViewModel.packageCount.getValue() == null || mViewModel.packageSum.getValue() == -1) {
				return;
			}
			// TODO:补充一个异常的逻辑
			tvSum.setText("/" + mViewModel.packageSum.getValue());
			tvCount.setText(String.valueOf(integer));
			// 计算进度百分比
			final int progress = (int) (((double) mViewModel.packageCount.getValue() / mViewModel.packageSum.getValue()) * 100);
			// 使用 Handler 更新进度条
			handler.post(() -> progressBar.setProgress(progress));
			// 完成状态
			if (Objects.equals(integer, mViewModel.packageSum.getValue()) && integer != -1) {
				tvTitle.setText(R.string.sendSuccess);
				setLottieAnimation("cat.json");
			} else if (Objects.equals(0, mViewModel.packageSum.getValue())) {
				// 空文件
				tvTitle.setText(R.string.sendAbort);
				Toast.makeText(getContext(), "文件为空", Toast.LENGTH_SHORT).show();
			} else {
				// 传输中
				tvTitle.setText(R.string.sending);
				tvSpeed.setText(MFrame.toSpeedString(mViewModel.packageCount.getValue(), startTime));
			}

		});
	}
	private void setLottieAnimation(String jsonFileName) {
		lottieAnimProgress.setAnimation(jsonFileName);  // 替换成新的动画文件
		lottieAnimProgress.playAnimation();  // 播放动画
	}
}
