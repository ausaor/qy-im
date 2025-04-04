import { v4 as uuidv4 } from 'uuid'

export const generateId = () => uuidv4()

// 可选：短ID生成
export const generateShortId = () => uuidv4().split('-')[0]