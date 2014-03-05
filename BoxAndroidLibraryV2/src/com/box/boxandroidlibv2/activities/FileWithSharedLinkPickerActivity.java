package com.box.boxandroidlibv2.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;
import com.box.boxandroidlibv2.R;
import com.box.boxandroidlibv2.dao.BoxAndroidFile;
import com.box.boxandroidlibv2.dao.BoxAndroidOAuthData;
import com.box.boxandroidlibv2.jsonparsing.AndroidBoxResourceHub;
import com.box.boxjavalibv2.dao.BoxFile;
import com.box.boxjavalibv2.dao.BoxSharedLinkAccess;
import com.box.boxjavalibv2.exceptions.AuthFatalFailureException;
import com.box.boxjavalibv2.exceptions.BoxJSONException;
import com.box.boxjavalibv2.exceptions.BoxServerException;
import com.box.boxjavalibv2.jsonparsing.BoxJSONParser;
import com.box.boxjavalibv2.requests.requestobjects.BoxFileRequestObject;
import com.box.boxjavalibv2.requests.requestobjects.BoxSharedLinkRequestObject;
import com.box.restclientv2.exceptions.BoxRestException;

public class FileWithSharedLinkPickerActivity extends FilePickerActivity {
	protected ProgressDialog mProgressDialog;

	@Override
	protected void handleFileClick(BoxAndroidFile file) {
		createSharedLinkAndFinish(file);
	}

	protected void createSharedLinkAndFinish(BoxAndroidFile boxAndroidFile) {
		try {
			mProgressDialog = ProgressDialog.show(
					this,
					getText(R.string.boxandroidlibv2_Creating_shared_link),
					getText(R.string.boxandroidlibv2_Please_wait));
		}
		catch (Exception e) {
			// WindowManager$BadTokenException will be caught and the app would not display
			// the 'Force Close' message
			mProgressDialog = null;
			return;
		}

		new AsyncTask<BoxAndroidFile, Void, BoxFile>() {
			@Override
			protected BoxFile doInBackground(BoxAndroidFile... params) {
				BoxSharedLinkRequestObject sharedLinkRequest =
						BoxSharedLinkRequestObject.createSharedLinkRequestObject(BoxSharedLinkAccess.OPEN);
				BoxFileRequestObject fileRequest =
						BoxFileRequestObject.createSharedLinkRequestObject(sharedLinkRequest);
				try {
					return mClient.getFilesManager().createSharedLink(params[0].getId(), fileRequest);
				} catch (BoxRestException e) {
					return null;
				} catch (BoxServerException e) {
					return null;
				} catch (AuthFatalFailureException e) {
					return null;
				}
			}

			@Override
			protected void onPostExecute(BoxFile boxFile) {
				if (mProgressDialog != null && mProgressDialog.isShowing())
					mProgressDialog.dismiss();

				BoxAndroidFile file = null;
				if (boxFile != null && boxFile.getSharedLink() != null) {
					BoxJSONParser jsonParser = new BoxJSONParser(new AndroidBoxResourceHub());
					try {
						// Convert BoxFile to BoxAndroidFile.
						file = jsonParser.parseIntoBoxObject(
								boxFile.toJSONString(jsonParser),
								BoxAndroidFile.class);
					} catch (BoxJSONException e) { /* Failure, do nothing. */ }
				}

				if (file != null) {
					// Success! Submit the file with the shared link.
					FileWithSharedLinkPickerActivity.super.handleFileClick(file);
				}
				else {
					// Failed to retrieve a valid file or shared link.
					Toast.makeText(
							FileWithSharedLinkPickerActivity.this,
							R.string.boxandroidlibv2_Problem_fetching_file,
							Toast.LENGTH_LONG).show();
				}
			}
		}.execute(boxAndroidFile);
	}

	public static Intent getLaunchIntent(Context context, final String folderId, final BoxAndroidOAuthData oauth, final String clientId,
	                                     final String clientSecret) {
		Intent intent = FolderNavigationActivity.getLaunchIntent(context, folderId, oauth, clientId, clientSecret);
		intent.setClass(context, FileWithSharedLinkPickerActivity.class);
		return intent;
	}
}
