import { defineStore } from 'pinia';


export default defineStore('uiStore', {
    state: () => ({
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
    }),
    actions: {

    },
    getters: {}
})