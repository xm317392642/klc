// This file was generated by PermissionsDispatcher. Do not modify!
package com.xr.ychat.login;

import android.support.v4.app.ActivityCompat;
import java.lang.Override;
import java.lang.String;
import java.lang.ref.WeakReference;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.PermissionUtils;

final class LoginAuthorizeActivityPermissionsDispatcher {
  private static final int REQUEST_QUERYAPPVERSIONREQUEST = 0;

  private static final String[] PERMISSION_QUERYAPPVERSIONREQUEST = new String[] {"android.permission.WRITE_EXTERNAL_STORAGE"};

  private LoginAuthorizeActivityPermissionsDispatcher() {
  }

  static void queryAppVersionRequestWithPermissionCheck(LoginAuthorizeActivity target) {
    if (PermissionUtils.hasSelfPermissions(target, PERMISSION_QUERYAPPVERSIONREQUEST)) {
      target.queryAppVersionRequest();
    } else {
      if (PermissionUtils.shouldShowRequestPermissionRationale(target, PERMISSION_QUERYAPPVERSIONREQUEST)) {
        target.showWhy(new LoginAuthorizeActivityQueryAppVersionRequestPermissionRequest(target));
      } else {
        ActivityCompat.requestPermissions(target, PERMISSION_QUERYAPPVERSIONREQUEST, REQUEST_QUERYAPPVERSIONREQUEST);
      }
    }
  }

  static void onRequestPermissionsResult(LoginAuthorizeActivity target, int requestCode,
      int[] grantResults) {
    switch (requestCode) {
      case REQUEST_QUERYAPPVERSIONREQUEST:
      if (PermissionUtils.verifyPermissions(grantResults)) {
        target.queryAppVersionRequest();
      } else {
        if (!PermissionUtils.shouldShowRequestPermissionRationale(target, PERMISSION_QUERYAPPVERSIONREQUEST)) {
          target.showNeverAskAgain();
        } else {
          target.showDenied();
        }
      }
      break;
      default:
      break;
    }
  }

  private static final class LoginAuthorizeActivityQueryAppVersionRequestPermissionRequest implements PermissionRequest {
    private final WeakReference<LoginAuthorizeActivity> weakTarget;

    private LoginAuthorizeActivityQueryAppVersionRequestPermissionRequest(LoginAuthorizeActivity target) {
      this.weakTarget = new WeakReference<LoginAuthorizeActivity>(target);
    }

    @Override
    public void proceed() {
      LoginAuthorizeActivity target = weakTarget.get();
      if (target == null) return;
      ActivityCompat.requestPermissions(target, PERMISSION_QUERYAPPVERSIONREQUEST, REQUEST_QUERYAPPVERSIONREQUEST);
    }

    @Override
    public void cancel() {
      LoginAuthorizeActivity target = weakTarget.get();
      if (target == null) return;
      target.showDenied();
    }
  }
}
