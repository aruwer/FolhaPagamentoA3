const tipoSelect = document.querySelector("#tipo");
const matriculaInput = document.querySelector("#matricula");

matriculaInput.addEventListener("input", () => {
    matriculaInput.value = matriculaInput.value.replace(/[^A-Za-z0-9]/g, "");
});

function atualizarCampos() {
    const tipo = tipoSelect.value;

    document.querySelectorAll("[data-type-fields]").forEach((grupo) => {
        const ativo = grupo.dataset.typeFields === tipo;
        grupo.hidden = !ativo;
        grupo.querySelectorAll("input").forEach((campo) => {
            campo.required = ativo && campo.hasAttribute("data-required-for-type");
        });
    });

    document.querySelectorAll("[data-type-hint]").forEach((aviso) => {
        aviso.hidden = aviso.dataset.typeHint !== tipo;
    });
}

tipoSelect.addEventListener("change", atualizarCampos);
atualizarCampos();