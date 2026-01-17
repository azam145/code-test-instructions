import React, { useState } from 'react';

const API_BASE_URL = 'http://localhost:8080';

function App() {
    const [currentPage, setCurrentPage] = useState('home');

    return (
        <>
            <header className="govuk-header" style={{ backgroundColor: '#0b0c0c', padding: '15px 0' }}>
                <div className="govuk-width-container">
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span style={{ fontSize: '24px', fontWeight: 'bold', color: '#ffffff' }}>
                            TPXimpact | URL Shortener
                        </span>
                        <nav>
                            <button
                                onClick={() => setCurrentPage('home')}
                                style={{
                                    background: 'none',
                                    border: 'none',
                                    color: currentPage === 'home' ? '#ffffff' : '#b1b4b6',
                                    fontSize: '16px',
                                    marginRight: '20px',
                                    cursor: 'pointer',
                                    textDecoration: currentPage === 'home' ? 'underline' : 'none',
                                    fontWeight: currentPage === 'home' ? 'bold' : 'normal'
                                }}
                            >
                                Home
                            </button>
                            <button
                                onClick={() => setCurrentPage('delete')}
                                style={{
                                    background: 'none',
                                    border: 'none',
                                    color: currentPage === 'delete' ? '#ffffff' : '#b1b4b6',
                                    fontSize: '16px',
                                    cursor: 'pointer',
                                    textDecoration: currentPage === 'delete' ? 'underline' : 'none',
                                    fontWeight: currentPage === 'delete' ? 'bold' : 'normal'
                                }}
                            >
                                Delete Alias
                            </button>
                        </nav>
                    </div>
                </div>
            </header>

            <div className="govuk-width-container">
                <main className="govuk-main-wrapper">
                    {currentPage === 'home' ? <HomePage /> : <DeletePage />}
                </main>
            </div>
        </>
    );
}

function HomePage() {
    const [fullUrl, setFullUrl] = useState('');
    const [customAlias, setCustomAlias] = useState('');
    const [result, setResult] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    const [copied, setCopied] = useState(false);

    const handleSubmit = (e) => {
        e.preventDefault();
        // 1. Basic Client-side Validation
        if (!fullUrl.startsWith('http')) {
            setError("URL must start with http:// or https://");
            return;
        }
        // 2. Alias Sanitization (Preventing malicious paths)
        const aliasRegex = /^[a-zA-Z0-9_-]*$/;
        if (customAlias && !aliasRegex.test(customAlias)) {
            setError("Alias can only contain letters, numbers, underscores, and hyphens.");
            return;
        }
        setLoading(true);
        setError(null);
        setResult(null);
        setCopied(false);

        fetch(`${API_BASE_URL}/shorten`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ fullUrl, customAlias }),
        })
            .then(response => response.json().then(data => ({ ok: response.ok, data })))
            .then(({ ok, data }) => {
                if (!ok) {
                    throw new Error(data.detail || 'Failed to shorten URL');
                }
                setResult(data);
                setFullUrl('');
                setCustomAlias('');
            })
            .catch(err => {
                setError(err.message);
            })
            .finally(() => {
                setLoading(false);
            });
    };

    const handleCopy = () => {
        navigator.clipboard.writeText(result.shortUrl)
            .then(() => {
                setCopied(true);
                setTimeout(() => setCopied(false), 2000);
            })
            .catch(err => {
                console.error('Failed to copy: ', err);
                alert("Could not copy automatically. Please select the text and press Ctrl+C.");
            });
    };

    return (
        <section>
            <h1 className="govuk-heading-l">Shorten a long URL</h1>
            <p className="govuk-body" style={{ fontSize: '19px', color: '#505a5f', marginBottom: '30px' }}>
                Create a shortened URL that's easy to share and remember.
            </p>

            {error && (
                <div className="govuk-error-summary" style={{
                    border: '4px solid #d4351c',
                    padding: '20px',
                    marginBottom: '30px'
                }}>
                    <h2 style={{ color: '#d4351c', marginTop: 0, fontSize: '24px' }}>
                        There is a problem
                    </h2>
                    <p style={{ marginBottom: 0 }}>{error}</p>
                </div>
            )}

            <div style={{ maxWidth: '600px' }}>
                <div className="govuk-form-group">
                    <label className="govuk-label" htmlFor="full-url" style={{ fontWeight: 'bold', fontSize: '19px' }}>
                        Enter the long URL
                    </label>
                    <span className="govuk-hint" style={{ display: 'block', marginBottom: '10px' }}>
                        For example, https://www.tpximpact.com/careers
                    </span>
                    <input
                        className="govuk-input"
                        id="full-url"
                        type="url"
                        value={fullUrl}
                        onChange={(e) => setFullUrl(e.target.value)}
                        required
                        style={{ width: '100%', fontSize: '16px', padding: '8px' }}
                    />
                </div>

                <div className="govuk-form-group">
                    <label className="govuk-label" htmlFor="custom-alias" style={{ fontWeight: 'bold', fontSize: '19px' }}>
                        Custom alias (optional)
                    </label>
                    <span className="govuk-hint" style={{ display: 'block', marginBottom: '10px' }}>
                        Must be unique, e.g., 'tpx-jobs'
                    </span>
                    <input
                        className="govuk-input"
                        id="custom-alias"
                        type="text"
                        value={customAlias}
                        onChange={(e) => setCustomAlias(e.target.value)}
                        style={{ width: '100%', fontSize: '16px', padding: '8px' }}
                    />
                </div>

                <button
                    className="govuk-button"
                    onClick={handleSubmit}
                    disabled={loading}
                    style={{
                        backgroundColor: '#00703c',
                        boxShadow: '0 2px 0 #005a30',
                        fontSize: '19px',
                        padding: '12px 20px'
                    }}
                >
                    {loading ? 'Processing...' : 'Shorten URL'}
                </button>
            </div>

            {result && (
                <div style={{
                    marginTop: '40px',
                    padding: '30px',
                    background: '#f3f2f1',
                    borderLeft: '10px solid #00703c',
                    maxWidth: '700px'
                }}>
                    <h3 style={{ marginTop: 0, fontSize: '24px', fontWeight: 'bold', color: '#00703c' }}>
                        ✓ Your shortened URL is ready
                    </h3>
                    <p style={{ fontSize: '24px', marginBottom: '20px', wordBreak: 'break-all' }}>
                        <a
                            href={result.shortUrl}
                            target="_blank"
                            rel="noreferrer"
                            style={{ color: '#1d70b8', textDecoration: 'underline' }}
                        >
                            {result.shortUrl}
                        </a>
                    </p>

                    <button
                        onClick={handleCopy}
                        className="govuk-button"
                        style={{
                            backgroundColor: copied ? '#005a30' : '#1d70b8',
                            boxShadow: copied ? '0 2px 0 #003618' : '0 2px 0 #003078',
                            fontSize: '19px',
                            padding: '12px 20px'
                        }}
                    >
                        {copied ? '✔ Copied to clipboard' : 'Copy link'}
                    </button>
                </div>
            )}
        </section>
    );
}

function DeletePage() {
    const [deleteAlias, setDeleteAlias] = useState('');
    const [deleteStatus, setDeleteStatus] = useState({ message: '', isError: false });
    const [deleteLoading, setDeleteLoading] = useState(false);

    const handleDelete = (e) => {
        e.preventDefault();
        setDeleteStatus({ message: '', isError: false });

        if (!deleteAlias.trim()) {
            setDeleteStatus({ message: 'Please enter an alias to delete', isError: true });
            return;
        }

        if (!window.confirm(`Are you sure you want to delete the alias "${deleteAlias}"?`)) {
            return;
        }

        setDeleteLoading(true);

        fetch(`${API_BASE_URL}/${deleteAlias}`, {
            method: 'DELETE',
        })
            .then(response => {
                if (response.status === 204) {
                    setDeleteStatus({
                        message: `Successfully deleted alias: ${deleteAlias}`,
                        isError: false
                    });
                    setDeleteAlias('');
                } else if (response.status === 404) {
                    setDeleteStatus({
                        message: 'Alias not found. Please check the alias and try again.',
                        isError: true
                    });
                } else {
                    return response.json().then(data => {
                        throw new Error(data.detail || 'Failed to delete');
                    });
                }
            })
            .catch(err => {
                setDeleteStatus({ message: err.message, isError: true });
            })
            .finally(() => {
                setDeleteLoading(false);
            });
    };

    return (
        <section>
            <h1 className="govuk-heading-l">Delete an alias</h1>
            <p className="govuk-body" style={{ fontSize: '19px', color: '#505a5f', marginBottom: '30px' }}>
                Remove a shortened URL alias permanently. This action cannot be undone.
            </p>

            {deleteStatus.message && (
                <div style={{
                    border: deleteStatus.isError ? '4px solid #d4351c' : '4px solid #00703c',
                    padding: '20px',
                    marginBottom: '30px',
                    backgroundColor: deleteStatus.isError ? '#f3f2f1' : '#e8f5e9',
                    maxWidth: '600px'
                }}>
                    <p style={{
                        margin: 0,
                        color: deleteStatus.isError ? '#d4351c' : '#00703c',
                        fontWeight: 'bold',
                        fontSize: '19px'
                    }}>
                        {deleteStatus.isError ? '✗ ' : '✓ '}
                        {deleteStatus.message}
                    </p>
                </div>
            )}

            <div style={{ maxWidth: '600px' }}>
                <div className="govuk-form-group">
                    <label className="govuk-label" htmlFor="delete-alias" style={{ fontWeight: 'bold', fontSize: '19px' }}>
                        Enter alias to delete
                    </label>
                    <span className="govuk-hint" style={{ display: 'block', marginBottom: '10px' }}>
                        For example, 'tpx-jobs'
                    </span>
                    <input
                        className="govuk-input"
                        id="delete-alias"
                        type="text"
                        value={deleteAlias}
                        onChange={(e) => setDeleteAlias(e.target.value)}
                        style={{ width: '100%', fontSize: '16px', padding: '8px' }}
                    />
                </div>

                <button
                    onClick={handleDelete}
                    className="govuk-button"
                    disabled={deleteLoading}
                    style={{
                        backgroundColor: '#d4351c',
                        boxShadow: '0 2px 0 #470b04',
                        fontSize: '19px',
                        padding: '12px 20px'
                    }}
                >
                    {deleteLoading ? 'Deleting...' : 'Delete alias'}
                </button>
            </div>

            <div style={{
                marginTop: '50px',
                padding: '20px',
                backgroundColor: '#f3f2f1',
                borderLeft: '5px solid #505a5f',
                maxWidth: '600px'
            }}>
                <h3 style={{ marginTop: 0, fontSize: '19px', fontWeight: 'bold' }}>
                    Need help?
                </h3>
                <p style={{ margin: 0 }}>
                    If you're having trouble deleting an alias, make sure you're entering the exact alias name without the full URL.
                </p>
            </div>
        </section>
    );
}

export default App;