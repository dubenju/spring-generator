// Fキー押下時の動作に要素を追加
var keyEvent = {
    "F12": ".btn-end" // 終了
};
$(function() {
});

/**
 * 画面生成処理
 */
function initialize() {
}

/**
 * 終了処理
 */
function end() {
    // ダイアログを表示する
    showDlg(DLG_TYPE.QUEST, DLG_BUTTON.YES_NO, msg_end, '').then(function(rtnVal) {
        if (rtnVal == DLG_RET.YES) {
            whidow.open('about:blank', '_self').close();
        } else if (rtnVal == DLG_RET.NO) {
        }
    });
}
