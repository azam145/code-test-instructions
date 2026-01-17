import React, { useState } from 'react';

const API_BASE_URL = 'http://localhost:8080';

function App() {
    const [fullUrl, setFullUrl] = useState('');
    const [customAlias, setCustomAlias] = useState('');
    const [result, setResult] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setResult(null);

        try {
            const response = await fetch(`${API_BASE_URL}/shorten`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ fullUrl, customAlias }),
            });

            const data = await response.json();

            if (!response.ok) {
                // Handle 409 Conflict or 400 Bad Request from our GlobalExceptionHandler
                throw new Error(data.detail || 'Failed to shorten URL');
            }

            setResult(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            <header className="govuk-header">
                <div className="govuk-width-container">
                    <span style={{ fontSize: '24px', fontWeight: 'bold' }}>TPXimpact | URL Shortener</span>
                </div>
            </header>

            <div className="govuk-width-container">
                <main className="govuk-main-wrapper">
                    <h1 className="govuk-heading-l">Shorten a long URL</h1>

                    {error && (
                        <div className="govuk-error-summary">
                            <h2 style={{ color: '#d4351c', marginTop: 0 }}>There is a problem</h2>
                            <p>{error}</p>
                        </div>
                    )}

                    <form onSubmit={handleSubmit}>
                        <div className="govuk-form-group">
                            <label className="govuk-label" htmlFor="full-url">Enter the long URL</label>
                            <span className="govuk-hint">For example, https://www.tpximpact.com/careers</span>
                            <input
                                className="govuk-input"
                                id="full-url"
                                type="url"
                                value={fullUrl}
                                onChange={(e) => setFullUrl(e.target.value)}
                                required
                            />
                        </div>

                        <div className="govuk-form-group">
                            <label className="govuk-label" htmlFor="custom-alias">Custom alias (optional)</label>
                            <span className="govuk-hint">Must be unique, e.g., 'tpx-jobs'</span>
                            <input
                                className="govuk-input"
                                id="custom-alias"
                                type="text"
                                value={customAlias}
                                onChange={(e) => setCustomAlias(e.target.value)}
                            />
                        </div>

                        <button className="govuk-button" type="submit" disabled={loading}>
                            {loading ? 'Processing...' : 'Shorten URL'}
                        </button>
                    </form>

                    {result && (
                        <div style={{ marginTop: '40px', padding: '20px', background: '#f3f2f1', borderLeft: '10px solid #00703c' }}>
                            <h3 className="govuk-label">Your shortened URL is ready:</h3>
                            <p style={{ fontSize: '24px' }}>
                                <a href={result.shortUrl} target="_blank" rel="noreferrer" style={{ color: '#1d70b8' }}>
                                    {result.shortUrl}
                                </a>
                            </p>
                            <button
                                onClick={() => navigator.clipboard.writeText(result.shortUrl)}
                                style={{ cursor: 'pointer', background: 'none', border: '1px solid black', padding: '5px' }}
                            >
                                Copy to clipboard
                            </button>
                        </div>
                    )}
                </main>
            </div>
        </>
    );
}

export default App;