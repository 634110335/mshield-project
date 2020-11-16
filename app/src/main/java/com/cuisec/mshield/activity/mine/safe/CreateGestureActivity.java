package com.cuisec.mshield.activity.mine.safe;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cuisec.mshield.MyApplication;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.lock.LockPatternUtils;
import com.cuisec.mshield.widget.lock.LockPatternView;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class CreateGestureActivity extends BaseActivity implements View.OnClickListener {

    private static final int ID_EMPTY_MESSAGE = -1;

    protected TextView mTipText;

    private LockPatternView mLockPatternView;

//    private Button mFooterRightButton;
//    private Button mFooterLeftButton;

    private Button mResetBtn;

    protected List<LockPatternView.Cell> mChosenPattern = null;

    private Stage mUiStage = Stage.Introduction;


    /**
     * The states of the left footer button.
     */
    private enum LeftButtonMode {
        Cancel(android.R.string.cancel, true), Retry(
                R.string.lockpattern_retry_button_text, true), Gone(
                ID_EMPTY_MESSAGE, false);

        /**
         * @param text    The displayed text for this mode.
         * @param enabled Whether the button should be enabled.
         */
        LeftButtonMode(int text, boolean enabled) {
            this.text = text;
            this.enabled = enabled;
        }

        final int text;
        final boolean enabled;
    }

    /**
     * The states of the right button.
     */
    private enum RightButtonMode {
        Continue(R.string.lockpattern_continue_button_text, true), ContinueDisabled(
                R.string.lockpattern_continue_button_text, false), Confirm(
                R.string.lockpattern_confirm_button_text, true), ConfirmDisabled(
                R.string.lockpattern_confirm_button_text, false), Ok(
                android.R.string.ok, true);

        /**
         * @param text    The displayed text for this mode.
         * @param enabled Whether the button should be enabled.
         */
        RightButtonMode(int text, boolean enabled) {
            this.text = text;
            this.enabled = enabled;
        }

        final int text;
        final boolean enabled;
    }


    /**
     * Keep track internally of where the user is in choosing a pattern.
     */
    private enum Stage {

        Introduction(R.string.lockpattern_recording_intro_header,
                LeftButtonMode.Cancel, RightButtonMode.ContinueDisabled,
                ID_EMPTY_MESSAGE, true), HelpScreen(
                R.string.lockpattern_settings_help_how_to_record,
                LeftButtonMode.Gone, RightButtonMode.Ok, ID_EMPTY_MESSAGE,
                false), ChoiceTooShort(
                R.string.lockpattern_recording_incorrect_too_short,
                LeftButtonMode.Retry, RightButtonMode.ContinueDisabled,
                ID_EMPTY_MESSAGE, true), FirstChoiceValid(
                R.string.lockpattern_pattern_entered_header,
                LeftButtonMode.Retry, RightButtonMode.Continue,
                ID_EMPTY_MESSAGE, false), NeedToConfirm(
                R.string.lockpattern_need_to_confirm, LeftButtonMode.Cancel,
                RightButtonMode.ConfirmDisabled, ID_EMPTY_MESSAGE, true), ConfirmWrong(
                R.string.lockpattern_need_to_unlock_wrong,
                LeftButtonMode.Cancel, RightButtonMode.ConfirmDisabled,
                ID_EMPTY_MESSAGE, true), ChoiceConfirmed(
                R.string.lockpattern_pattern_confirmed_header,
                LeftButtonMode.Cancel, RightButtonMode.Confirm,
                ID_EMPTY_MESSAGE, false);

        /**
         * @param headerMessage  The message displayed at the top.
         * @param leftMode       The mode of the left button.
         * @param rightMode      The mode of the right button.
         * @param footerMessage  The footer message.
         * @param patternEnabled Whether the pattern widget is enabled.
         */
        Stage(int headerMessage, LeftButtonMode leftMode,
              RightButtonMode rightMode, int footerMessage,
              boolean patternEnabled) {
            this.headerMessage = headerMessage;
            this.leftMode = leftMode;
            this.rightMode = rightMode;
            this.footerMessage = footerMessage;
            this.patternEnabled = patternEnabled;
        }

        final int headerMessage;
        final LeftButtonMode leftMode;
        final RightButtonMode rightMode;
        final int footerMessage;
        final boolean patternEnabled;
    }


    @Override
    protected void setContentView() {

        setContentView(R.layout.activity_create_gesture);
    }

    @Override
    protected void initializeViews() {

        showTitle("创建手势密码");

        mTipText = (TextView) findViewById(R.id.mine_security_create_tip_tv);

        mLockPatternView = (LockPatternView) findViewById(R.id.mine_security_create_lock_pattern);
        mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
        mLockPatternView.setTactileFeedbackEnabled(false);

//        mFooterRightButton = (Button)findViewById(R.id.right_btn);
//        mFooterLeftButton = (Button)findViewById(R.id.reset_btn);
//        mFooterRightButton.setOnClickListener(this);
//        mFooterLeftButton.setOnClickListener(this);

        mResetBtn = (Button) findViewById(R.id.mine_security_create_btn);
        mResetBtn.setOnClickListener(this);
    }

    @Override
    protected void initializeData() {

    }

    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };

    protected LockPatternView.OnPatternListener mChooseNewLockPatternListener = new LockPatternView.OnPatternListener() {

        public void onPatternStart() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
            patternInProgress();
        }

        public void onPatternCleared() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }

        public void onPatternDetected(List<LockPatternView.Cell> pattern) {
            if (pattern == null)
                return;
            // Log.i("way", "result = " + pattern.toString());
            if (mUiStage == Stage.NeedToConfirm
                    || mUiStage == Stage.ConfirmWrong) {
                if (mChosenPattern == null)
                    throw new IllegalStateException(
                            "null chosen pattern in stage 'need to confirm");
                if (mChosenPattern.equals(pattern)) {
                    updateStage(Stage.ChoiceConfirmed);
                } else {
                    updateStage(Stage.ConfirmWrong);
                }
            } else if (mUiStage == Stage.Introduction
                    || mUiStage == Stage.ChoiceTooShort) {
                if (pattern.size() < LockPatternUtils.MIN_LOCK_PATTERN_SIZE) {
                    updateStage(Stage.ChoiceTooShort);
                } else {
                    mChosenPattern = new ArrayList<>(
                            pattern);
                    updateStage(Stage.FirstChoiceValid);
                }
            } else {
                throw new IllegalStateException("Unexpected stage " + mUiStage
                        + " when " + "entering the pattern.");
            }
        }

        public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {

        }

        private void patternInProgress() {
            mTipText.setText(R.string.lockpattern_recording_inprogress);
//            mFooterLeftButton.setEnabled(false);
//            mFooterRightButton.setEnabled(false);
        }
    };


    private void updateStage(Stage stage) {
        mUiStage = stage;
        if (stage == Stage.ChoiceTooShort) {
            mTipText.setText(getResources().getString(stage.headerMessage,
                    LockPatternUtils.MIN_LOCK_PATTERN_SIZE));
        } else {
            mTipText.setText(stage.headerMessage);
        }

//        if (stage.leftMode == LeftButtonMode.Gone) {
//            mResetBtn.setVisibility(View.GONE);
//        } else {
//            mResetBtn.setVisibility(View.VISIBLE);
//            mResetBtn.setText(stage.leftMode.text);
//            mResetBtn.setEnabled(stage.leftMode.enabled);
//        }
//
//        mResetBtn.setText(stage.rightMode.text);
//        mResetBtn.setEnabled(stage.rightMode.enabled);

        // same for whether the patten is enabled
        if (stage.patternEnabled) {
            mLockPatternView.enableInput();
        } else {
            mLockPatternView.disableInput();
        }

        mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Correct);

        switch (mUiStage) {
            case Introduction:
                mLockPatternView.clearPattern();
                break;
            case HelpScreen:
//                mLockPatternView.setPattern(LockPatternView.DisplayMode.Animate, mAnimatePattern);
                break;
            case ChoiceTooShort:
                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                postClearPatternRunnable();
                break;
            case FirstChoiceValid:
                updateStage(Stage.NeedToConfirm);
                mResetBtn.setVisibility(View.VISIBLE);
                break;
            case NeedToConfirm:
                mLockPatternView.clearPattern();
//                updatePreviewViews();
                break;
            case ConfirmWrong:
                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                postClearPatternRunnable();
                break;
            case ChoiceConfirmed:
                saveChosenPatternAndFinish();
                break;
        }

    }

    // clear the wrong pattern unless they have started a new one
    // already
    private void postClearPatternRunnable() {
        mLockPatternView.removeCallbacks(mClearPatternRunnable);
        mLockPatternView.postDelayed(mClearPatternRunnable, 2000);
    }


    @Override
    public void onClick(View v) {

        mChosenPattern = null;
        mLockPatternView.clearPattern();
        updateStage(Stage.Introduction);
        mResetBtn.setVisibility(View.GONE);
    }

    private void saveChosenPatternAndFinish() {

        T.showShort(this, "手势密码设置成功");

        MyApplication.getInstance().getLockPatternUtils().saveLockPattern(mChosenPattern);

        Intent resultIntent = new Intent();
        this.setResult(RESULT_OK, resultIntent);
        this.finish();
    }
}
