// Utility Functions
const Utils = {
    // Format currency
    formatCurrency(amount, currency = 'INR') {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: currency,
            minimumFractionDigits: 2
        }).format(amount);
    },
    
    // Format date
    formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-IN', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    },
    
    // Format date and time
    formatDateTime(dateString) {
        const date = new Date(dateString);
        return date.toLocaleString('en-IN', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    },
    
    // Show error message
    showError(elementId, message) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = message;
            element.style.display = 'block';
            setTimeout(() => {
                element.style.display = 'none';
            }, 5000);
        }
    },
    
    // Show success message
    showSuccess(elementId, message) {
        const element = document.getElementById(elementId);
        if (element) {
            element.innerHTML = message;
            element.style.display = 'block';
            setTimeout(() => {
                element.style.display = 'none';
            }, 5000);
        }
    },
    
    // Validate account number
    isValidAccountNumber(accountNumber) {
        return /^ACC\d{6}$/.test(accountNumber);
    },
    
    // Validate amount
    isValidAmount(amount) {
        return amount > 0 && !isNaN(amount);
    }
};

// API Service
const API = {
    baseURL: '/api',
    
    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };
        
        try {
            const response = await fetch(url, config);
            const data = await response.json();
            
            if (!response.ok) {
                throw new Error(data.message || 'Request failed');
            }
            
            return data;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    },
    
    // Auth endpoints
    async login(email, password) {
        return this.request('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
    },
    
    // Account endpoints
    async getBalance(accountNumber) {
        return this.request(`/accounts/${accountNumber}/balance`);
    },
    
    // Transfer endpoints
    async transfer(transferData) {
        return this.request('/transfers', {
            method: 'POST',
            body: JSON.stringify(transferData)
        });
    },
    
    async getTransactionHistory(accountNumber) {
        return this.request(`/transfers/history/${accountNumber}`);
    }
};

// Make utilities globally available
window.Utils = Utils;
window.API = API;

// Console log for debugging
console.log('QuickPay Money Transfer System - Iteration 1 (Monolithic)');
console.log('Application loaded successfully');