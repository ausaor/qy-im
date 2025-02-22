const emoTextList = ['emo憨笑', 'emo媚眼', 'emo开心', 'emo坏笑', 'emo可怜', 'emo爱心', 'emo笑哭', 'emo拍手',
	'emo惊喜', 'emo打气', 'emo大哭', 'emo流泪', 'emo饥饿', 'emo难受', 'emo健身', 'emo示爱', 'emo色色', 'emo眨眼',
	'emo暴怒', 'emo惊恐', 'emo思考', 'emo头晕', 'emo大吐', 'emo酷笑', 'emo翻滚', 'emo享受', 'emo鼻涕', 'emo快乐',
	'emo雀跃', 'emo微笑', 'emo贪婪', 'emo红心', 'emo粉心', 'emo星星', 'emo大火', 'emo眼睛', 'emo音符', 'emo叹号',
	'emo问号', 'emo绿叶', 'emo燃烧', 'emo喇叭', 'emo警告', 'emo信封', 'emo房子', 'emo礼物', 'emo点赞', 'emo举手',
	'emo拍手', 'emo点头', 'emo摇头', 'emo偷瞄', 'emo庆祝', 'emo疾跑', 'emo打滚', 'emo惊吓', 'emo起跳'
];


let transform = (content, extClass) => {
	return content.replace(/\#[a-z\u4E00-\u9FA5]{1,6}\;/gi, (emoText)=>{
		// 将匹配结果替换表情图片
		let word = emoText.replace(/\#|\;/gi, '');
		let idx = emoTextList.indexOf(word);
		if (idx == -1) {
			return emoText;
		}
		let path = textToPath(emoText);
		let img = `<img src="${path}" class="${extClass}"/>`;
		return img;
	});
}



let textToPath = (emoText) => {
	let word = emoText.replace(/\#|\;/gi, '');
	let idx = emoTextList.indexOf(word);
	let baseUrl = "/"
	// #ifdef H5
		baseUrl = window.location.pathname;
	// #endif
	return `${baseUrl}static/emoji/${idx}.gif`;
}



export default {
	emoTextList,
	transform,
	textToPath
}