import { reactive } from 'vue'

type DialogType = 'alert' | 'confirm' | 'prompt'

export const dialogState = reactive({
  visible: false,
  type: 'alert' as DialogType,
  title: '',
  message: '',
  input: '',
  placeholder: '',
  resolve: null as ((val?: any) => void) | null,
  reject: null as ((err?: any) => void) | null
})

export function alertDialog(message: string, title = '提示') {
  return new Promise<void>((resolve) => {
    dialogState.type = 'alert'
    dialogState.title = title
    dialogState.message = message
    dialogState.input = ''
    dialogState.visible = true
    dialogState.resolve = () => {
      dialogState.visible = false
      resolve()
    }
  })
}

export function confirmDialog(message: string, title = '确认'): Promise<boolean> {
  return new Promise((resolve) => {
    dialogState.type = 'confirm'
    dialogState.title = title
    dialogState.message = message
    dialogState.input = ''
    dialogState.visible = true
    dialogState.resolve = (val = false) => {
      dialogState.visible = false
      resolve(Boolean(val))
    }
  })
}

export function promptDialog(message: string, placeholder = '', title = '输入'): Promise<string | null> {
  return new Promise((resolve) => {
    dialogState.type = 'prompt'
    dialogState.title = title
    dialogState.message = message
    dialogState.placeholder = placeholder
    dialogState.input = ''
    dialogState.visible = true
    dialogState.resolve = (val: any) => {
      dialogState.visible = false
      resolve(val == null ? null : String(val))
    }
  })
}


