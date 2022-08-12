import * as React from 'react'
import { useApp, useRendererContext } from '~hooks'
import { TLTargetType } from '@tldraw/core'
import type { TLReactCustomEvents } from '~types'

export function useKeyboardEvents(ref: React.RefObject<HTMLDivElement>) {
  const app = useApp()
  const { callbacks } = useRendererContext()
  const shiftKeyDownRef = React.useRef(false)

  React.useEffect(() => {
    const onKeyDown: TLReactCustomEvents['keyboard'] = e => {
      callbacks.onKeyDown?.({ type: TLTargetType.Canvas, order: -1 }, e)
      shiftKeyDownRef.current = e.shiftKey
    }

    const onKeyUp: TLReactCustomEvents['keyboard'] = e => {
      callbacks.onKeyUp?.({ type: TLTargetType.Canvas, order: -1 }, e)
      shiftKeyDownRef.current = e.shiftKey
    }

    const onPaste = (e: ClipboardEvent) => {
      if (!app.editingShape && ref.current?.contains(document.activeElement)) {
        e.preventDefault()
        app.paste(e, shiftKeyDownRef.current)
      }
    }

    const onCopy = (e: ClipboardEvent) => {
      if (
        !app.editingShape &&
        app.selectedShapes.size > 0 &&
        ref.current?.contains(document.activeElement)
      ) {
        e.preventDefault()
        app.copy()
      }
    }

    window.addEventListener('keydown', onKeyDown)
    window.addEventListener('keyup', onKeyUp)
    document.addEventListener('paste', onPaste)
    document.addEventListener('copy', onCopy)
    return () => {
      window.removeEventListener('keydown', onKeyDown)
      window.removeEventListener('keyup', onKeyUp)
      document.removeEventListener('paste', onPaste)
      document.removeEventListener('copy', onCopy)
    }
  }, [])
}