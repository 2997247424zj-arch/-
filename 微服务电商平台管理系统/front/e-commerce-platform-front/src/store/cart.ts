import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCartListAPI, updateCartAPI, deleteCartAPI, deleteCartBatchAPI } from '@/api/modules/cart'
import type { CartItem } from '@/types'

export const useCartStore = defineStore('cart', () => {
  const cartList = ref<CartItem[]>([])

  const fetchCartList = async () => {
    const res: any = await getCartListAPI()
    if (res.data) {
      cartList.value = res.data || []
    }
  }

  const selectedItems = computed(() => {
    return cartList.value.filter((item) => item.isSelected === 1)
  })

  // Total price for selected items
  const totalPrice = computed(() => {
    return selectedItems.value.reduce((total, item) => total + item.price * item.quantity, 0)
  })

  const totalCount = computed(() => {
    return selectedItems.value.reduce((count, item) => count + item.quantity, 0)
  })

  const updateQuantity = async (id: number, quantity: number) => {
    await updateCartAPI({ id, quantity })
    await fetchCartList()
  }

  const toggleSelect = async (id: number, isSelected: number) => {
    const item = cartList.value.find((i) => i.id === id)
    if (item) {
      await updateCartAPI({ id, quantity: item.quantity, isSelected })
      await fetchCartList()
    }
  }

  const removeItems = async (ids: number[]) => {
    if (ids.length === 0) return

    if (ids.length === 1) {
      await deleteCartAPI(ids[0] as number)
    } else {
      await deleteCartBatchAPI(ids)
    }
    await fetchCartList()
  }

  const clearCartState = () => {
    cartList.value = []
  }

  return {
    cartList,
    selectedItems,
    totalPrice,
    totalCount,
    fetchCartList,
    updateQuantity,
    toggleSelect,
    removeItems,
    clearCartState,
  }
})
