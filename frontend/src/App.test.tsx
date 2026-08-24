import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import App from './App'

describe('App', () => {
    it('renders the welcome screen', () => {
        render(<App />)

        expect(
            screen.getByRole('heading', { name: 'Documentation Integrity' }),
        ).toBeInTheDocument()
        expect(
            screen.getByText('Welcome to the frontend foundation.'),
        ).toBeInTheDocument()
    })
})