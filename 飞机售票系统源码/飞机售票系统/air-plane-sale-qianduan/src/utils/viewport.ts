// Utility to initialize intersection-based animations and a lightweight parallax effect
export function initViewportEffects(): { destroy: () => void } {
  const ioOptions = { threshold: 0.12 }
  const inViewObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      const el = entry.target as HTMLElement
      if (entry.isIntersecting) {
        el.classList.add('in-view')
      } else {
        // keep visible once entered; comment this line to only trigger once
        // el.classList.remove('in-view')
      }
    })
  }, ioOptions)

  const animateEls = Array.from(document.querySelectorAll('.animate-on-scroll'))
  animateEls.forEach(el => inViewObserver.observe(el))

  // Parallax: elements with data-parallax attribute (value is multiplier)
  const parallaxEls = Array.from(document.querySelectorAll<HTMLElement>('[data-parallax]'))
  let ticking = false

  function updateParallax() {
    parallaxEls.forEach(el => {
      const rect = el.getBoundingClientRect()
      const multiplier = parseFloat(el.dataset.parallax || '0.06')
      const offset = (window.innerHeight - rect.top) * multiplier
      el.style.setProperty('--parallax-offset', `${offset}px`)
    })
    ticking = false
  }

  function onScroll() {
    if (!ticking) {
      window.requestAnimationFrame(updateParallax)
      ticking = true
    }
  }

  window.addEventListener('scroll', onScroll, { passive: true })
  // Initial update
  updateParallax()

  return {
    destroy() {
      animateEls.forEach(el => inViewObserver.unobserve(el))
      window.removeEventListener('scroll', onScroll)
      inViewObserver.disconnect()
    }
  }
}


