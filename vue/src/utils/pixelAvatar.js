const palette = ['#ff6700', '#ffb347', '#2f4858', '#5b8c85', '#f7c8a0', '#f8fafc']

export function createSeed() {
  if (window.crypto && window.crypto.getRandomValues) {
    const values = new Uint32Array(2)
    window.crypto.getRandomValues(values)
    return values[0] ^ values[1]
  }
  return Date.now() ^ Math.floor(Math.random() * 0xffffffff)
}

export function createPixelAvatar(seed) {
  let value = Number(seed) || createSeed()
  const random = () => {
    value = (value * 1664525 + 1013904223) >>> 0
    return value / 0x100000000
  }

  const background = palette[Math.floor(random() * palette.length)]
  const foreground = palette[Math.floor(random() * (palette.length - 1))]
  const cells = []

  for (let row = 0; row < 8; row += 1) {
    for (let column = 0; column < 4; column += 1) {
      if (random() > 0.5) {
        cells.push(`<rect x="${column}" y="${row}" width="1" height="1" fill="${foreground}"/>`)
        cells.push(`<rect x="${7 - column}" y="${row}" width="1" height="1" fill="${foreground}"/>`)
      }
    }
  }

  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 8 8" shape-rendering="crispEdges"><rect width="8" height="8" fill="${background}"/>${cells.join('')}</svg>`
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
}

export function createPixelAvatarId(seed) {
  return `pixel:${Number(seed) || createSeed()}`
}
