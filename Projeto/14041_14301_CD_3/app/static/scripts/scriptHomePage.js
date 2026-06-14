document.addEventListener('DOMContentLoaded', () => {
    const productsContainer = document.querySelector('.products');
    const logoutBtn = document.getElementById('logout-btn');
    const accountBtn = document.getElementById('account-btn');
    const cartBtn = document.getElementById('cart-btn');
    const searchBtn = document.getElementById('search-btn');
    const searchInput = document.getElementById('search-input');

    // Função para carregar produtos
    async function loadProducts() {
        try {
            const response = await fetch('/produtos');
            const products = await response.json();

            products.forEach((product, index) => {
                const productElement = document.createElement('div');
                productElement.classList.add('product');
                productElement.innerHTML = `
                    <a href="/product/${index}">
                        <img src="./static/images/produtos/${product.image}" alt="${product.name}" title="Clique para mais informações">
                    </a>
                    <h2>${product.name}</h2>
                    <p>${product.price}</p>
                    <button data-name="${product.name}">Adicionar ao Carrinho</button>
                `;
                productsContainer.appendChild(productElement);
            });

            // Adicionar evento aos botões "Adicionar ao Carrinho"
            document.querySelectorAll('.product button').forEach(button => {
                button.addEventListener('click', async (e) => {
                    const productName = e.target.dataset.name;

                    try {
                        const response = await fetch('/add_to_cart', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({ product_name: productName })
                        });

                        const result = await response.json();
                        if (response.ok) {
                            alert(result.message);
                        } else {
                            alert(result.error);
                        }
                    } catch (error) {
                        console.error('Erro ao adicionar ao carrinho:', error);
                    }
                });
            });
        } catch (error) {
            console.error('Erro ao carregar os produtos:', error);
        }
    }

    searchBtn.addEventListener('click', (e) => {
        e.preventDefault();  // Previne o comportamento padrão do botão
        performSearch();
    });

    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();  // Previne o comportamento padrão do enter
            performSearch();
        }
    });

    // Mostrar botão "Admin" se o utilizador for admin
    async function checkAdmin() {
        try {
            const res = await fetch('/get_session');
            const data = await res.json();

            if (data.admin) {
                const adminBtn = document.createElement('button');
                adminBtn.textContent = 'Admin';
                adminBtn.style.backgroundColor = '#ffc107'; // amarelo
                adminBtn.style.color = '#000';
                adminBtn.style.marginLeft = '10px';
                adminBtn.onclick = () => window.location.href = '/admin';

                const actionsDiv = document.querySelector('.account-actions');
                actionsDiv.appendChild(adminBtn);
            }
        } catch (e) {
            console.error('Erro ao verificar se é admin:', e);
        }
    }

    checkAdmin(); // chama a função no load


    function performSearch() {
        const query = searchInput.value.trim().toLowerCase();
        if (query) {
            // Filtrar produtos com base na pesquisa
            const productElements = document.querySelectorAll('.product');
            productElements.forEach(productElement => {
                const productName = productElement.querySelector('h2').textContent.toLowerCase();
                if (productName.includes(query)) {
                    productElement.style.display = 'block';
                } else {
                    productElement.style.display = 'none';
                }
            });
        } else {
            // Mostrar todos os produtos se a pesquisa estiver vazia
            const productElements = document.querySelectorAll('.product');
            productElements.forEach(productElement => productElement.style.display = 'block');
        }
    }

    // Lógica para redirecionar para a página da conta
    accountBtn.addEventListener('click', () => {
        window.location.href = '/account';
    });

    // Lógica para redirecionar para o carrinho
    cartBtn.addEventListener('click', () => {
        window.location.href = '/cart';
    });

    // Lógica de logout
    logoutBtn.addEventListener('click', () => {
        window.location.href = '/doLogout';
    });
    

    // Carregar produtos ao carregar a página
    loadProducts();
});
