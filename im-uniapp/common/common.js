/**
 * 处理@用户
 * @param {String} content 输入内容
 * @param {Array} atUserIds @用户ID列表
 * @returns string 处理后的内容
 */
export let processAtUsers = (content, atUserIds)  => {
    // 匹配 @用户名#{数字} 的模式
    const atPattern = /(@[^#\s]+)#\{(\d+)\}/g;

    return content.replace(atPattern, (match, username, userId) => {
        // 将userId转换为数字进行比较
        const id = parseInt(userId, 10);

        // 如果用户ID在atUserIds数组中，则去掉#{数字}部分
        if (atUserIds.includes(id)) {
            return username;
        }

        // 如果不在数组中，保留原有内容
        return match;
    });
}