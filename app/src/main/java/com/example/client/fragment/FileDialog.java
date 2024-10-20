package com.example.client.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.client.R;
import com.example.client.utils.MBinaryFileManager;
import com.example.client.viewmodel.MainViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class FileDialog extends DialogFragment {
	private MaterialAlertDialogBuilder builder;
	private MainViewModel mViewModel;
	private ProgressBar progressBar;
	private TextView tvSum;
	private TextView tvCount;
	private TextView tvTitle;
	private Handler handler;

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		handler = new Handler(Looper.getMainLooper());
		// 创建并返回对话框
		initViews();
		if (getActivity() != null) {
			mViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
		}
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
		return builder.create();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initObserver();
	}

	private void initViews() {
		// 创建LayoutInflater对象
		LayoutInflater inflater = requireActivity().getLayoutInflater();

		// 加载自定义布局
		View dialogView = inflater.inflate(R.layout.dialog, null);
		builder = new MaterialAlertDialogBuilder(requireContext());
		// 设置自定义布局
		builder.setView(dialogView);

		progressBar = dialogView.findViewById(R.id.progressBar);
		tvSum = dialogView.findViewById(R.id.tvSum);
		tvCount = dialogView.findViewById(R.id.tvCount);
		tvTitle = dialogView.findViewById(R.id.tvTitle);
	}

	private void sendFile() {
		new Thread(() -> {
			// 大文件传输包拆分和拼接的Manager
			MBinaryFileManager mBinaryFileManager = new MBinaryFileManager(mViewModel.outFileUriPath);
			// 发送大文件之前需要进行的一些初始化
			int totalPackageNum = mBinaryFileManager.getTotalNum();
			mViewModel.packageSum.setValue(totalPackageNum);
			mViewModel.mSocketClient.initSend();
			while (mBinaryFileManager.has_next()) {
				mViewModel.mSocketClient.sendSyn(mBinaryFileManager, () -> {
					int curPackageNum = mBinaryFileManager.getCurNum();
					mViewModel.packageCount.setValue(curPackageNum);
					return null;
				});
			}
		}).start();
	}

	private void receiveFile() {
		new Thread(() -> {
			// 大文件传输包拆分和拼接的Manager
			MBinaryFileManager mBinaryFileManager = new MBinaryFileManager(mViewModel.outFileUriPath);
			// 发送大文件之前需要进行的一些初始化
			int totalPackageNum = mBinaryFileManager.getTotalNum();
			mViewModel.packageSum.setValue(totalPackageNum);
			mViewModel.mSocketClient.initSend();
			while (mBinaryFileManager.has_next()) {
				mViewModel.mSocketClient.sendSyn(mBinaryFileManager, () -> {
					int curPackageNum = mBinaryFileManager.getCurNum();
					mViewModel.packageCount.setValue(curPackageNum);
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
			tvSum.setText(String.valueOf(mViewModel.packageSum.getValue()));
			tvCount.setText(String.valueOf(integer));
			// 计算进度百分比
			if (mViewModel.packageCount.getValue() == null || mViewModel.packageSum.getValue() == null) {
				return;
			}
			final int progress = (int) (((double) mViewModel.packageCount.getValue() / mViewModel.packageSum.getValue()) * 100);
			// 使用 Handler 更新 UI
			handler.post(() -> progressBar.setProgress(progress));
			// 当全部文件接收完毕，关闭弹窗
			if (mViewModel.packageCount == mViewModel.packageSum) {
				dismiss();
			}
		});
	}
}
