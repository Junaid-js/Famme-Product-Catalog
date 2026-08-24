'use strict';

function getCatalogClientId() {
    try {
        let clientId = sessionStorage.getItem('catalogClientId');
        if (!clientId) {
            clientId = `editor-${Math.random().toString(36).slice(2, 8)}`;
            sessionStorage.setItem('catalogClientId', clientId);
        }
        return clientId;
    } catch {
        return 'editor-unknown';
    }
}
window.getCatalogClientId = getCatalogClientId;

document.addEventListener('htmx:sseMessage', (event) => {
    const banner = document.getElementById('product-conflict-banner');
    if (!banner || !banner.dataset.productId) {
        return;
    }

    let change;
    try {
        change = JSON.parse(event.detail.data);
    } catch {
        return;
    }

    if (String(change.productId) !== banner.dataset.productId) {
        return;
    }
    if (change.actorClientId && change.actorClientId === getCatalogClientId()) {
        return;
    }

    banner.querySelector('[data-conflict-message]').textContent = change.type === 'product-deleted'
        ? `"${change.title}" was deleted by another editor.`
        : `"${change.title}" was just updated by another editor. Reload to see the latest version.`;
    banner.hidden = false;
});

function collapseProductVariants(button) {
    const row = document.getElementById(button.getAttribute('aria-controls'));
    if (row) {
        row.hidden = true;
    }
    button.setAttribute('aria-expanded', 'false');
    button.title = 'Show variants';
}

function reindexProductVariantFields() {
    document.querySelectorAll('#product-variant-fields .catalog-new-variant').forEach((row, index) => {
        row.querySelector('[data-variant-number]').textContent = `Variant ${index + 1}`;
        row.querySelectorAll('[data-variant-field]').forEach((field) => {
            field.setAttribute('name', `variants[${index}].${field.dataset.variantField}`);
        });
    });
}

function addProductVariantField() {
    const fields = document.getElementById('product-variant-fields');
    const template = document.getElementById('product-variant-template');
    fields.append(template.content.cloneNode(true));
    reindexProductVariantFields();
    fields.lastElementChild.querySelector('[data-variant-field="title"]').focus();
}

function removeProductVariantField(button) {
    button.closest('.catalog-new-variant').remove();
    reindexProductVariantFields();
}

document.addEventListener('click', (event) => {
    const button = event.target.closest?.('.catalog-variant-trigger');
    if (!button) {
        return;
    }

    if (button.getAttribute('aria-expanded') === 'true') {
        event.preventDefault();
        event.stopImmediatePropagation();
        collapseProductVariants(button);
        return;
    }

    document
        .querySelectorAll('.catalog-variant-trigger[aria-expanded="true"]')
        .forEach(collapseProductVariants);

    button.setAttribute('aria-expanded', 'true');
    button.title = 'Hide variants';
}, true);
