const HISTORY_KEY = 'history'
export function getHistoryList(): string[] {
  return JSON.parse(localStorage.getItem(HISTORY_KEY) || '[]')
}
/** 添加历史记录，超过 10 条就删除 1 条，已存在就移动到首位，否则直接放在首位 */
export function addHistory (history: string) {
  const historyList = getHistoryList()
  const index = historyList.indexOf(history)
  if (index > -1) {
    historyList.splice(index, 1)
  }
  historyList.unshift(history)
  if (historyList.length > 10) {
    historyList.pop()
  }
  localStorage.setItem(HISTORY_KEY, JSON.stringify(historyList))
}
