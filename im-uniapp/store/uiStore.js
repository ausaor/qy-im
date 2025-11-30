import { defineStore } from 'pinia';


export default defineStore('uiStore', {
    state: () => {
        return {
            navBar: {
                themes: [
                    {
                        index: 0,
                        name: '深海晨曦',
                        background: 'linear-gradient(135deg, #003973 0%, #00a1ff 100%)'
                    },
                    {
                        index: 1,
                        name: '极光暮色',
                        background: 'linear-gradient(135deg, #2c3e50 0%, #4ca1af 100%)'
                    },
                    {
                        index: 2,
                        name: '落日熔金',
                        background: 'linear-gradient(135deg, #8e2de2 0%, #f1a102 100%)'
                    },
                    {
                        index: 3,
                        name: '森林秘境',
                        background: 'linear-gradient(135deg, #134e5e 0%, #71b280 100%)'
                    },
                    {
                        index: 4,
                        name: '胭脂绯红',
                        background: 'linear-gradient(135deg, #aa076b 0%, #f57862 100%)'
                    },
                    {
                        index: 5,
                        name: '星辰夜空',
                        background: 'linear-gradient(135deg, #141e30 0%, #2c5364 100%)'
                    },
                    {
                        index: 6,
                        name: '琥珀流光',
                        background: 'linear-gradient(135deg, #ffc371 0%, #ff6b6b 100%)'
                    },
                    {
                        index: 7,
                        name: '青瓷釉色',
                        background: 'linear-gradient(135deg, #1d976c 0%, #93f9b9 100%)'
                    },
                    {
                        index: 8,
                        name: '玫瑰黄昏',
                        background: 'linear-gradient(135deg, #7a49a5 0%, #f0a6ca 100%)'
                    },
                    {
                        index: 9,
                        name: '湖水碧波',
                        background: 'linear-gradient(135deg, #0074bd 0%, #5de6ff 100%)'
                    },
                    {
                        index: 10,
                        name: '午夜紫霞',
                        background: 'linear-gradient(135deg, #4B0082 0%, #9370DB 100%)'
                    },
                    {
                        index: 11,
                        name: '秋日枫叶',
                        background: 'linear-gradient(135deg, #8B0000 0%, #FFA07A 100%)'
                    },
                    {
                        index: 12,
                        name: '翡翠梦境',
                        background: 'linear-gradient(135deg, #006400 0%, #98FB98 100%)'
                    },
                    {
                        index: 13,
                        name: '蔚蓝海岸',
                        background: 'linear-gradient(135deg, #000080 0%, #87CEEB 100%)'
                    },
                    {
                        index: 14,
                        name: '暮光之城',
                        background: 'linear-gradient(135deg, #2F4F4F 0%, #D3D3D3 100%)'
                    },
                    {
                        index: 15,
                        name: '金盏花田',
                        background: 'linear-gradient(135deg, #DAA520 0%, #FFD700 100%)'
                    },
                    {
                        index: 16,
                        name: '冰川雪莲',
                        background: 'linear-gradient(135deg, #191970 0%, #87CEFA 100%)'
                    },
                    {
                        index: 17,
                        name: '珊瑚海湾',
                        background: 'linear-gradient(135deg, #FF6347 0%, #FFA07A 100%)'
                    },
                    {
                        index: 18,
                        name: '薰衣草丘',
                        background: 'linear-gradient(135deg, #9932CC 0%, #BA55D3 100%)'
                    },
                    {
                        index: 19,
                        name: '碧玺晶石',
                        background: 'linear-gradient(135deg, #2E8B57 0%, #66CDAA 100%)'
                    }
                ],
                currentIndex: 0
            },
            chatBubble: {
                bubbles: [
                    {
                        name: '清新薄荷',
                        background: 'linear-gradient(135deg, #e0f7fa 0%, #b2ebf2 100%)',
                        color: '#00695c'
                    },
                    {
                        name: '温柔樱花',
                        background: 'linear-gradient(135deg, #f8bbd0 0%, #f48fb1 100%)',
                        color: '#880e4f'
                    },
                    {
                        name: '宁静天空',
                        background: 'linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%)',
                        color: '#0d47a1'
                    },
                    {
                        name: '优雅薰衣',
                        background: 'linear-gradient(135deg, #ede7f6 0%, #d1c4e9 100%)',
                        color: '#4527a0'
                    },
                    {
                        name: '温暖橙阳',
                        background: 'linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%)',
                        color: '#e65100'
                    },
                    {
                        name: '清新竹叶',
                        background: 'linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%)',
                        color: '#1b5e20'
                    },
                    {
                        name: '浪漫紫罗兰',
                        background: 'linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%)',
                        color: '#4a148c'
                    },
                    {
                        name: '纯净白云',
                        background: 'linear-gradient(135deg, #fafafa 0%, #eeeeee 100%)',
                        color: '#212121'
                    },
                    {
                        name: '海洋之心',
                        background: 'linear-gradient(135deg, #e1f5fe 0%, #b3e5fc 100%)',
                        color: '#01579b'
                    },
                    {
                        name: '甜美蜜桃',
                        background: 'linear-gradient(135deg, #ffe0b2 0%, #ffcc80 100%)',
                        color: '#e65100'
                    }
                ],
                currentBubbleIndex: 0
            }
        }
    },
    actions: {

    },
    getters: {}
})