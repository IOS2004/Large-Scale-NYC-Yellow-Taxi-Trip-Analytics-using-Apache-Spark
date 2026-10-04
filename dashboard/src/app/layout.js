import './globals.css'

export const metadata = {
  title: 'NYC Taxi Analytics Dashboard',
  description: 'Premium insights into NYC Taxi Big Data',
}

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  )
}
