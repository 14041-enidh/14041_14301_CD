document.addEventListener('DOMContentLoaded', async () => {
    async function loadCart() {
        try {
            const response = await fetch('/get_cart');
            const cartData = await response.json();
            const cartItemsContainer = document.getElementById('cart-items');

            cartItemsContainer.innerHTML = ''; // Limpa os itens do carrinho antes de renderizar

            if (cartData.produtos && cartData.produtos.length > 0) {
                cartData.produtos.forEach((product, index) => {
                    const productItem = document.createElement('div');
                    productItem.classList.add('product-item');
                    productItem.innerHTML = `
                        <span>${product.name}</span>
                        <span>${product.price}</span>
                        <button class="remove-btn" data-index="${index}">Remover</button>
                    `;
                    cartItemsContainer.appendChild(productItem);
                });

                // Adicionar evento aos botões "Remover"
                document.querySelectorAll('.remove-btn').forEach(button => {
                    button.addEventListener('click', async (e) => {
                        const productIndex = e.target.dataset.index;

                        try {
                            const removeResponse = await fetch('/remove_from_cart', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/json'
                                },
                                body: JSON.stringify({ index: productIndex })
                            });

                            if (removeResponse.ok) {
                                await loadCart(); // Recarrega os dados do carrinho
                            } else {
                                console.error('Erro ao remover produto do carrinho:', await removeResponse.text());
                            }
                        } catch (error) {
                            console.error('Erro ao processar remoção:', error);
                        }
                    });
                });
            } else {
                cartItemsContainer.innerHTML = '<p>Seu carrinho está vazio.</p>';
            }
        } catch (error) {
            console.error('Erro ao carregar os produtos do carrinho:', error);
        }
    }

    // Carrega o carrinho ao inicializar a página
    await loadCart();

    document.getElementById('checkout-btn').addEventListener('click', () => {
        const paymentMethod = document.getElementById('payment-method').value;
        alert(`Forma de pagamento selecionada: ${paymentMethod}`);
        // Aqui você pode adicionar lógica para processar o pagamento.
    });
});
