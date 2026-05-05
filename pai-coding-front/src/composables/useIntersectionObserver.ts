import { ref, onMounted, onBeforeUnmount, type Ref } from 'vue'

export function useIntersectionObserver(
  elementRef: Ref<Element | null>,
  options?: IntersectionObserverInit
) {
  const isVisible = ref(false)
  let observer: IntersectionObserver | null = null

  onMounted(() => {
    if (!elementRef.value) return
    observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          isVisible.value = true
          observer?.unobserve(entry.target)
        }
      },
      { threshold: 0.1, ...options }
    )
    observer.observe(elementRef.value)
  })

  onBeforeUnmount(() => {
    observer?.disconnect()
  })

  return { isVisible }
}
