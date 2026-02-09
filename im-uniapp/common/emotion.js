import UNI_APP from '@/.env.js'

const emoTextList = ['emo憨笑', 'emo媚眼', 'emo开心', 'emo坏笑', 'emo可怜', 'emo爱心', 'emo笑哭', 'emo拍手',
	'emo惊喜', 'emo打气', 'emo大哭', 'emo流泪', 'emo饥饿', 'emo难受', 'emo健身', 'emo示爱', 'emo色色', 'emo眨眼',
	'emo暴怒', 'emo惊恐', 'emo思考', 'emo头晕', 'emo大吐', 'emo酷笑', 'emo翻滚', 'emo享受', 'emo鼻涕', 'emo快乐',
	'emo雀跃', 'emo微笑', 'emo贪婪', 'emo红心', 'emo粉心', 'emo星星', 'emo大火', 'emo眼睛', 'emo音符', 'emo叹号',
	'emo问号', 'emo绿叶', 'emo燃烧', 'emo喇叭', 'emo警告', 'emo信封', 'emo房子', 'emo礼物', 'emo点赞', 'emo举手',
	'emo拍手', 'emo点头', 'emo摇头', 'emo偷瞄', 'emo庆祝', 'emo疾跑', 'emo打滚', 'emo惊吓', 'emo起跳'
];

const originalEmoTextList = ['微笑', '撇嘴', '色', '发呆', '得意', '流泪', '害羞', '闭嘴', '睡', '大哭', '尴尬', '发怒', '调皮',
	'呲牙', '惊讶', '难过', '酷', '冷汗', '抓狂', '吐', '偷笑', '可爱', '白眼', '傲慢', '饥饿', '困', '惊恐', '流汗', '憨笑', '大兵',
	'奋斗', '咒骂', '疑问', '嘘', '晕', '折磨', '衰', '骷髅', '敲打', '再见', '擦汗', '抠鼻', '鼓掌', '糗大了', '坏笑', '左哼哼', '右哼哼',
	'哈欠', '鄙视', '委屈', '快哭了', '阴险', '亲亲', '吓', '可怜', '菜刀', '西瓜', '啤酒', '篮球', '乒乓', '咖啡', '饭', '猪头', '玫瑰',
	'凋谢', '示爱', '爱心', '心碎', '蛋糕', '闪电', '炸弹', '刀', '足球', '瓢虫', '便便', '月亮', '太阳', '礼物', '拥抱', '强', '弱', '握手',
	'胜利', '抱拳', '勾引', '拳头', '差劲', '爱你', 'NO', 'OK', '爱情', '飞吻', '跳跳', '发抖', '怄火', '转圈', '磕头', '回头', '跳绳', '挥手',
	'激动', '街舞', '献吻', '左太极', '右太极'];


let transform = (content, extClass) => {
	return content.replace(/\#[a-z\u4E00-\u9FA5]{1,6}\;/gi, (emoText)=>{
		// 将匹配结果替换表情图片
		let word = emoText.replace(/\#|\;/gi, '');
		let idx = emoTextList.indexOf(word);
		if (idx >= 0) {
			let path = textToPath(emoText);
			let img = `<img src="${path}" class="${extClass}" alt="${word}"/>`;
			return img;
		}
		idx = originalEmoTextList.indexOf(word);
		if (idx >= 0) {
			let path = textToPathOriginal(emoText);
			let img = `<img src="${path}" class="${extClass}" alt="${word}"/>`;
			return img;
		}
		if (idx == -1) {
			return emoText;
		}
	});
}

let transformOriginal = (content, extClass) => {
	return content.replace(/\#[a-z\u4E00-\u9FA5]{1,6}\;/gi, (emoText)=>{
		// 将匹配结果替换表情图片
		let word = emoText.replace(/\#|\;/gi, '');
		let idx = originalEmoTextList.indexOf(word);
		if (idx >= 0) {
			let path = textToPathOriginal(emoText);
			let img = `<img src="${path}" class="${extClass}" alt="${word}"/>`;
			return img;
		}
		if (idx == -1) {
			return emoText;
		}
	});
}


let textToPath = (emoText) => {
	let word = emoText.replace(/\#|\;/gi, '');
	let idx = emoTextList.indexOf(word);
	return UNI_APP.EMO_URL2 + idx + ".gif";
	// let baseUrl = "/"
	// // #ifdef H5
	// 	baseUrl = window.location.pathname;
	// // #endif
	// return `${baseUrl}static/emoji/emo2/${idx}.gif`;
}

let textToPathOriginal = (emoText) => {
	let word = emoText.replace(/\#|\;/gi, '');
	let idx = originalEmoTextList.indexOf(word);
	return UNI_APP.EMO_URL1 + idx + ".gif";
	// let baseUrl = "/"
	// // #ifdef H5
	// baseUrl = window.location.pathname;
	// // #endif
	// return `${baseUrl}static/emoji/emo1/${idx}.gif`;
}

export default {
	emoTextList,
	originalEmoTextList,
	transform,
	transformOriginal,
	textToPath,
	textToPathOriginal,
}