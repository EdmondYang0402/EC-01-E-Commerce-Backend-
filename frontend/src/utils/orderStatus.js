export const ORDER_STATUS = Object.freeze({ PENDING_PAYMENT: 0, PAID: 1, SHIPPED: 2, COMPLETED: 3, CANCELLED: 4 })
const aliases = { PENDING_PAYMENT: 0, PAID: 1, SHIPPED: 2, COMPLETED: 3, CANCELLED: 4 }
export const normalizeOrderStatus = (status) => {
  if (typeof status === 'string' && status in aliases) return aliases[status]
  const value = Number(status)
  return Number.isInteger(value) && value >= 0 && value <= 4 ? value : -1
}
export const orderStatusKey = (status) => `order.status.${normalizeOrderStatus(status)}`
