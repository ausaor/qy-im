import { MessageBox } from "element-ui";

/**
 * 确认框
 * @param {String} params.text 提示文本
 * @param {Function} params.methods 确认后的回调函数
 * @returns
 */
let isConfirm = (params) => {
    return MessageBox.confirm(params.text, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
    })
        .then(() => {
            params.methods && params.methods();
        })
        .catch(() => { });
};

export default {
    isConfirm,
}